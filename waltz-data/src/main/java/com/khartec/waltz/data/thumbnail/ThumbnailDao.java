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

package com.khartec.waltz.data.thumbnail;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.thumbnail.ImmutableThumbnail;
import com.khartec.waltz.model.thumbnail.Thumbnail;
import com.khartec.waltz.schema.tables.records.ThumbnailRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Thumbnail.THUMBNAIL;


@Repository
public class ThumbnailDao {


    private final DSLContext dsl;


    public static RecordMapper<? super Record, Thumbnail> TO_DOMAIN_MAPPER = r -> {
        ThumbnailRecord record = r.into(ThumbnailRecord.class);

        EntityReference parentRef = EntityReference.mkRef(
                EntityKind.valueOf(record.getParentEntityKind()),
                record.getParentEntityId());

        return ImmutableThumbnail.builder()
                .parentEntityReference(parentRef)
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .mimeType(record.getMimeType())
                .blob(record.getBlob())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .build();
    };


    public static Function<Thumbnail, ThumbnailRecord> TO_RECORD_MAPPER = ea -> {
        ThumbnailRecord record = new ThumbnailRecord();

        record.setParentEntityKind(ea.parentEntityReference().kind().name());
        record.setParentEntityId(ea.parentEntityReference().id());
        record.setLastUpdatedAt(Timestamp.valueOf(ea.lastUpdatedAt()));
        record.setLastUpdatedBy(ea.lastUpdatedBy());
        record.setMimeType(ea.mimeType());
        record.setBlob(ea.blob());
        record.setExternalId(ea.externalId().orElse(null));
        record.setProvenance(ea.provenance());
        return record;
    };


    @Autowired
    public ThumbnailDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Thumbnail getByReference(EntityReference reference) {
        return dsl.selectFrom(THUMBNAIL)
                .where(THUMBNAIL.PARENT_ENTITY_KIND.eq(reference.kind().name()))
                .and(THUMBNAIL.PARENT_ENTITY_ID.eq(reference.id()))
                .fetchOne(TO_DOMAIN_MAPPER);

    }


    public int create(Thumbnail thumbnail) {
        ThumbnailRecord record = TO_RECORD_MAPPER.apply(thumbnail);
        return dsl.insertInto(THUMBNAIL)
                .set(record)
                .execute();
    }


    public boolean update(Thumbnail thumbnail) {
        ThumbnailRecord record = TO_RECORD_MAPPER.apply(thumbnail);
        record.changed(THUMBNAIL.PARENT_ENTITY_KIND, false);
        record.changed(THUMBNAIL.PARENT_ENTITY_ID, false);
        return dsl.executeUpdate(record) == 1;
    }


    public int deleteByReference(EntityReference ref) {
        return dsl.deleteFrom(THUMBNAIL)
                .where(THUMBNAIL.PARENT_ENTITY_ID.eq(ref.id()))
                .and(THUMBNAIL.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .execute();
    }

}
