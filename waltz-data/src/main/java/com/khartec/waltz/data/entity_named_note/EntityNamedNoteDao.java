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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.UserTimestamp;
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

    
    
    public boolean save(EntityReference ref, long namedNoteTypeId, String noteText, UserTimestamp lastUpdate) {
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
