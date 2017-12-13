/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

        return dsl.executeUpdate(record) == 1;
    }


    public EntityNamedNodeType getById(long namedNoteTypeId) {
        return dsl.selectFrom(ENTITY_NAMED_NOTE_TYPE)
                .where(ENTITY_NAMED_NOTE_TYPE.ID.eq(namedNoteTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

}
