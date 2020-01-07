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

package com.khartec.waltz.data.perspective_rating;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.perspective.ImmutablePerspectiveRating;
import com.khartec.waltz.model.perspective.ImmutablePerspectiveRatingValue;
import com.khartec.waltz.model.perspective.PerspectiveRating;
import com.khartec.waltz.model.perspective.PerspectiveRatingValue;
import com.khartec.waltz.schema.tables.Measurable;
import com.khartec.waltz.schema.tables.records.PerspectiveRatingRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.ArrayUtilities.sum;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.PerspectiveRating.PERSPECTIVE_RATING;

@Repository
public class PerspectiveRatingDao {

    private static final RecordMapper<Record, PerspectiveRating> TO_DOMAIN_MAPPER = record -> {
        PerspectiveRatingRecord r = record.into(PERSPECTIVE_RATING);

        PerspectiveRatingValue rating = ImmutablePerspectiveRatingValue.builder()
                .measurableX(r.getMeasurableX())
                .measurableY(r.getMeasurableY())
                .rating(firstChar(r.getRating(), 'Z'))
                .build();

        return ImmutablePerspectiveRating.builder()
                .value(rating)
                .entityReference(mkRef(
                        EntityKind.valueOf(r.getEntityKind()),
                        r.getEntityId()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PerspectiveRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PerspectiveRating> findForEntity(
            long categoryX,
            long categoryY,
            EntityReference ref) {

        Measurable mX = MEASURABLE.as("mX");
        Measurable mY = MEASURABLE.as("mY");

        Condition mXJoinCondition = mX.ID.eq(PERSPECTIVE_RATING.MEASURABLE_X);
        Condition mYJoinCondition = mY.ID.eq(PERSPECTIVE_RATING.MEASURABLE_Y);

        Condition categoriesMatch = mX.MEASURABLE_CATEGORY_ID.eq(categoryX)
                .and(mY.MEASURABLE_CATEGORY_ID.eq(categoryY));

        Condition entityMatches = PERSPECTIVE_RATING.ENTITY_KIND.eq(ref.kind().name())
                .and(PERSPECTIVE_RATING.ENTITY_ID.eq(ref.id()));

        return dsl
                .select(PERSPECTIVE_RATING.fields())
                .from(PERSPECTIVE_RATING)
                .innerJoin(mX)
                .on(mXJoinCondition)
                .innerJoin(mY)
                .on(mYJoinCondition)
                .where(entityMatches.and(categoriesMatch))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int remove(EntityReference ref, Set<PerspectiveRatingValue> removals) {
        Set<PerspectiveRatingRecord> records = SetUtilities.map(removals, item -> {
            PerspectiveRatingRecord record = new PerspectiveRatingRecord();
            record.setEntityKind(ref.kind().name());
            record.setEntityId(ref.id());
            record.setMeasurableX(item.measurableX());
            record.setMeasurableY(item.measurableY());
            return record;
        });

        int[] rc = dsl
                .batchDelete(records)
                .execute();
        return sum(rc);
    }


    public int add(EntityReference ref, Set<PerspectiveRatingValue> additions, String username) {
        Set<PerspectiveRatingRecord> records = SetUtilities.map(additions, item -> {
            PerspectiveRatingRecord record = new PerspectiveRatingRecord();
            record.setEntityKind(ref.kind().name());
            record.setEntityId(ref.id());
            record.setMeasurableX(item.measurableX());
            record.setMeasurableY(item.measurableY());
            record.setRating(Character.toString(item.rating()));
            record.setLastUpdatedAt(Timestamp.valueOf(nowUtc()));
            record.setLastUpdatedBy(username);
            return record;
        });

        int[] rc = dsl
                .batchStore(records)
                .execute();

        return sum(rc);
    }


    public int cascadeRemovalOfMeasurableRating(EntityReference entityReference, long measurableId) {
        Condition measurableMatchesEitherAxis = PERSPECTIVE_RATING.MEASURABLE_X.eq(measurableId)
                .or(PERSPECTIVE_RATING.MEASURABLE_Y.eq(measurableId));

        return dsl.deleteFrom(PERSPECTIVE_RATING)
                .where(PERSPECTIVE_RATING.ENTITY_ID.eq(entityReference.id()))
                .and(PERSPECTIVE_RATING.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(measurableMatchesEitherAxis)
                .execute();
    }


    public Collection<PerspectiveRating> findForEntity(EntityReference ref) {
        return dsl
                .select(PERSPECTIVE_RATING.fields())
                .from(PERSPECTIVE_RATING)
                .where(PERSPECTIVE_RATING.ENTITY_ID.eq(ref.id()))
                .and(PERSPECTIVE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
