/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.measurable_rating;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.measurable_rating.ImmutableMeasurableRating;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

@Repository
public class MeasurableRatingDao {

    private static final RecordMapper<? super MeasurableRatingRecord, MeasurableRating> TO_DOMAIN_MAPPER = record -> {
        MeasurableRatingRecord r = record.into(MEASURABLE_RATING);
        return ImmutableMeasurableRating.builder()
                .entityReference(EntityReference.mkRef(
                        EntityKind.valueOf(r.getEntityKind()),
                        r.getEntityId()))
                .description(r.getDescription())
                .provenance(r.getProvenance())
                .rating(RagRating.valueOf(r.getRating()))
                .measurableId(r.getMeasurableId())
                .lastUpdatedAt(toLocalDateTime(r.getLastUpdatedAt()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<MeasurableRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl
                .selectFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
