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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.entity_named_note.EntityNamedNote;
import com.khartec.waltz.model.entity_named_note.ImmutableEntityNamedNote;
import com.khartec.waltz.schema.tables.records.EntityNamedNoteRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;

@Repository
public class EntityNamedNoteDao {

    private static final RecordMapper<EntityNamedNoteRecord, EntityNamedNote> TO_DOMAIN_MAPPER = r -> {
        return ImmutableEntityNamedNote
                .builder()
                .entityReference(EntityReference.mkRef(
                        EntityKind.valueOf(r.getEntityKind()),
                        r.getEntityId()))
                .namedNoteTypeId(r.getNamedNoteTypeId())
                .noteText(r.getNoteText())
                .provenance(r.getProvenance())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityNamedNoteDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityNamedNote> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return dsl
                .selectFrom(ENTITY_NAMED_NOTE)
                .where(ENTITY_NAMED_NOTE.ENTITY_KIND.eq(ref.kind().name()))
                .and(ENTITY_NAMED_NOTE.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }

    
    
    public boolean save(EntityReference ref, long namedNoteTypeId, String noteText, LastUpdate lastUpdate) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(lastUpdate, "lastUpdate cannot be null");

        EntityNamedNoteRecord record = dsl.newRecord(ENTITY_NAMED_NOTE);

        record.setEntityId(ref.id());
        record.setEntityKind(ref.kind().name());
        record.setNoteText(noteText);
        record.setNamedNoteTypeId(namedNoteTypeId);
        record.setLastUpdatedAt(Timestamp.valueOf(lastUpdate.at()));
        record.setLastUpdatedBy(lastUpdate.by());
        record.setProvenance("waltz");

        if (dsl.executeUpdate(record) == 1) {
            return true;
        } else {
            return dsl.executeInsert(record) == 1;
        }
    }

    public boolean remove(EntityReference ref, long namedNoteTypeId) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.deleteFrom(ENTITY_NAMED_NOTE)
                .where(ENTITY_NAMED_NOTE.ENTITY_KIND.eq(ref.kind().name()))
                .and(ENTITY_NAMED_NOTE.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_NAMED_NOTE.NAMED_NOTE_TYPE_ID.eq(namedNoteTypeId))
                .execute() == 1;
    }

    
    public boolean remove(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.deleteFrom(ENTITY_NAMED_NOTE)
                .where(ENTITY_NAMED_NOTE.ENTITY_KIND.eq(ref.kind().name()))
                .and(ENTITY_NAMED_NOTE.ENTITY_ID.eq(ref.id()))
                .execute() > 0;
    }
}
