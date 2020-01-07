/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.data.entity_named_note;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import com.khartec.waltz.model.entity_named_note.ImmutableEntityNamedNodeType;
import com.khartec.waltz.schema.tables.records.EntityNamedNoteTypeRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.join;
import static com.khartec.waltz.common.StringUtilities.splitThenMap;
import static com.khartec.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;
import static com.khartec.waltz.schema.tables.EntityNamedNoteType.ENTITY_NAMED_NOTE_TYPE;

@Repository
public class EntityNamedNoteTypeDao {

    private static final String SEPARATOR = ";";


    private static final RecordMapper<EntityNamedNoteTypeRecord, EntityNamedNodeType> TO_DOMAIN_MAPPER = r -> {
        List<EntityKind> applicableEntityKinds = splitThenMap(
                r.getApplicableEntityKinds(),
                SEPARATOR,
                EntityKind::valueOf);

        return ImmutableEntityNamedNodeType
                .builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .addAllApplicableEntityKinds(applicableEntityKinds)
                .isReadOnly(r.getIsReadonly())
                .position(r.getPosition())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityNamedNoteTypeDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityNamedNodeType> findAll() {
        return dsl
                .selectFrom(ENTITY_NAMED_NOTE_TYPE)
                .fetch(TO_DOMAIN_MAPPER);
    }


    /**
     * Attempts to remove the named note type specified by the
     * given id.  The removal will only take place if no
     * entity named notes refer to this id.
     *
     * @param id
     * @return boolean - whether the note was removed.
     */
    public boolean removeById(Long id) {

        Select anyUsageOfType = DSL
                .select(ENTITY_NAMED_NOTE.ENTITY_ID)
                .from(ENTITY_NAMED_NOTE)
                .where(ENTITY_NAMED_NOTE.NAMED_NOTE_TYPE_ID.eq(id));

        return dsl
                .deleteFrom(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.ID.eq(id))
                .andNotExists(anyUsageOfType)
                .execute() == 1;
    }


    /**
     * Creates a new record and returns the generated id.  All fields in the command must be
     * provided.
     * @param command
     * @return
     */
    public long create(EntityNamedNoteTypeChangeCommand command) {
        String name = Checks.checkOptionalIsPresent(command.name(), "Name must be provided");
        Set<EntityKind> applicableEntityKinds = Checks.checkOptionalIsPresent(command.applicableEntityKinds(), "Applicable Entity Kinds must be provided");
        String kinds = join(applicableEntityKinds, SEPARATOR);

        EntityNamedNoteTypeRecord record = dsl.newRecord(ENTITY_NAMED_NOTE_TYPE);
        record.setName(name);
        record.setDescription(command.description().orElse(""));
        record.setApplicableEntityKinds(kinds);
        record.setIsReadonly(command.isReadOnly().orElse(false));
        record.setPosition(command.position().orElse(0));

        record.store();

        return record.getId();
    }


    public boolean update(long id, EntityNamedNoteTypeChangeCommand command) {
        EntityNamedNoteTypeRecord record = new EntityNamedNoteTypeRecord();
        record.set(ENTITY_NAMED_NOTE_TYPE.ID, id);
        record.changed(ENTITY_NAMED_NOTE_TYPE.ID, false);

        command.name()
                .ifPresent(name -> record.setName(name));
        command.description()
                .ifPresent(desc -> record.setDescription(desc));
        command.applicableEntityKinds()
                .ifPresent(kinds -> record.setApplicableEntityKinds(join(kinds, SEPARATOR)));
        command.isReadOnly()
                .ifPresent(readOnly -> record.setIsReadonly(readOnly));
        command.position()
                .ifPresent(position -> record.setPosition(position));

        return dsl.executeUpdate(record) == 1;
    }


    public EntityNamedNodeType getById(long namedNoteTypeId) {
        return dsl.selectFrom(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.ID.eq(namedNoteTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

}
