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

package org.finos.waltz.data.entity_named_note;

import org.finos.waltz.schema.tables.records.EntityNamedNoteTypeRecord;
import org.finos.waltz.common.Checks;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.entity_named_note.EntityNamedNodeType;
import org.finos.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import org.finos.waltz.model.entity_named_note.ImmutableEntityNamedNodeType;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;
import static org.finos.waltz.schema.tables.EntityNamedNoteType.ENTITY_NAMED_NOTE_TYPE;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.common.StringUtilities.splitThenMap;

@Repository
public class EntityNamedNoteTypeDao {

    private static final String SEPARATOR = ";";


    private static final RecordMapper<Record, EntityNamedNodeType> TO_DOMAIN_MAPPER = record -> {

        EntityNamedNoteTypeRecord r = record.into(ENTITY_NAMED_NOTE_TYPE);

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
                .externalId(Optional.ofNullable(r.getExternalId()))
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
                .select(ENTITY_NAMED_NOTE_TYPE.fields())
                .from(ENTITY_NAMED_NOTE_TYPE)
                .fetch(TO_DOMAIN_MAPPER);
    }


    /**
     * Attempts to remove the named note type specified by the
     * given id.  The removal will only take place if no
     * entity named notes refer to this id.
     *
     * @param id - identifier of the named note type to remove
     * @return boolean - whether the note was removed.
     */
    public boolean removeById(Long id) {

        SelectConditionStep<Record1<Long>> anyUsageOfType = DSL
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
     * @param command an EntityNameNoteTypeChangeCommand object
     * @return the id of the note type created.
     */
    public long create(EntityNamedNoteTypeChangeCommand command) {
        String name = Checks.checkOptionalIsPresent(command.name(), "Name must be provided");
        Set<EntityKind> applicableEntityKinds = Checks.checkOptionalIsPresent(command.applicableEntityKinds(), "Applicable Entity Kinds must be provided");
        String kinds = join(applicableEntityKinds, SEPARATOR);

        EntityNamedNoteTypeRecord record = dsl.newRecord(ENTITY_NAMED_NOTE_TYPE);
        record.setName(name);
        record.setExternalId(command.externalId().orElse(""));
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
                .ifPresent(record::setName);
        command.externalId()
                .ifPresent(record::setExternalId);
        command.description()
                .ifPresent(record::setDescription);
        command.applicableEntityKinds()
                .ifPresent(kinds -> record.setApplicableEntityKinds(join(kinds, SEPARATOR)));
        command.isReadOnly()
                .ifPresent(record::setIsReadonly);
        command.position()
                .ifPresent(record::setPosition);

        return dsl.executeUpdate(record) == 1;
    }


    public EntityNamedNodeType getById(long namedNoteTypeId) {
        return dsl.select(ENTITY_NAMED_NOTE_TYPE.fields())
                .from(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.ID.eq(namedNoteTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public EntityNamedNodeType getByExternalId(String externalId) {
        return dsl
                .select(ENTITY_NAMED_NOTE_TYPE.fields())
                .from(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }
}
