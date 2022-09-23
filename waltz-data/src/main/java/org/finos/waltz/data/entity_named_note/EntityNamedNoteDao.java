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

import org.finos.waltz.schema.tables.records.EntityNamedNoteRecord;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.model.entity_named_note.ImmutableEntityNamedNote;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import static org.finos.waltz.schema.Tables.ENTITY_NAMED_NOTE_TYPE;
import static org.finos.waltz.schema.tables.EntityNamedNote.ENTITY_NAMED_NOTE;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class EntityNamedNoteDao {

    private static final RecordMapper<Record, EntityNamedNote> TO_DOMAIN_MAPPER = record -> {
        EntityNamedNoteRecord r = record.into(ENTITY_NAMED_NOTE);
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
                .select(ENTITY_NAMED_NOTE.fields())
                .from(ENTITY_NAMED_NOTE)
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

    public Set<EntityNamedNote> findByNoteTypeExtId(String noteTypeExtId) {
        return dsl
                .select(ENTITY_NAMED_NOTE.fields())
                .from(ENTITY_NAMED_NOTE)
                .innerJoin(ENTITY_NAMED_NOTE_TYPE).on(ENTITY_NAMED_NOTE.NAMED_NOTE_TYPE_ID.eq(ENTITY_NAMED_NOTE_TYPE.ID))
                .where(ENTITY_NAMED_NOTE_TYPE.EXTERNAL_ID.eq(noteTypeExtId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public Set<EntityNamedNote> findByNoteTypeExtIdAndEntityReference(String noteTypeExtId, EntityReference entityReference) {
        return dsl
                .select(ENTITY_NAMED_NOTE.fields())
                .from(ENTITY_NAMED_NOTE)
                .innerJoin(ENTITY_NAMED_NOTE_TYPE).on(ENTITY_NAMED_NOTE.NAMED_NOTE_TYPE_ID.eq(ENTITY_NAMED_NOTE_TYPE.ID))
                .where(ENTITY_NAMED_NOTE_TYPE.EXTERNAL_ID.eq(noteTypeExtId))
                .and(ENTITY_NAMED_NOTE.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(ENTITY_NAMED_NOTE.ENTITY_ID.eq(entityReference.id()))
                .fetchSet(TO_DOMAIN_MAPPER);
    }
}
