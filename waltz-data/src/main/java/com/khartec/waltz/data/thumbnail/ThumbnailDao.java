/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
