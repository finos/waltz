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

package com.khartec.waltz.data.rating_scheme;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rating.ImmutableRagName;
import com.khartec.waltz.model.rating.ImmutableRatingScheme;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.RatingSchemeItem;
import com.khartec.waltz.schema.tables.records.RatingSchemeItemRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.schema.Tables.ASSESSMENT_DEFINITION;
import static com.khartec.waltz.schema.Tables.ASSESSMENT_RATING;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.RatingScheme.RATING_SCHEME;
import static com.khartec.waltz.schema.tables.RatingSchemeItem.RATING_SCHEME_ITEM;

@Repository
public class RatingSchemeDAO {

    public static final RatingSchemeItem CONSTRAINING_RATING = Tables.RATING_SCHEME_ITEM.as("constrainingRating");

    public static final Field<Boolean> IS_RESTRICTED_FIELD = DSL.coalesce(
            DSL.field(Tables.RATING_SCHEME_ITEM.POSITION.lt(CONSTRAINING_RATING.POSITION)), false)
            .as("isRestricted");


    public static final RecordMapper<Record, RagName> TO_ITEM_MAPPER = record -> {

        RatingSchemeItemRecord r = record.into(RATING_SCHEME_ITEM);

        ImmutableRagName.Builder builder = ImmutableRagName.builder()
                .id(r.getId())
                .ratingSchemeId(r.getSchemeId())
                .name(r.getName())
                .rating(firstChar(r.getCode(), 'X'))
                .userSelectable(r.getUserSelectable())
                .color(r.getColor())
                .position(r.getPosition())
                .description(r.getDescription());

        if (record.field(IS_RESTRICTED_FIELD) != null){
            builder.isRestricted(record.get(IS_RESTRICTED_FIELD));
        }

        return builder.build();
    };


    public static final RecordMapper<RatingSchemeRecord, RatingScheme> TO_SCHEME_MAPPER = r ->
        ImmutableRatingScheme.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();


    private final DSLContext dsl;


    @Autowired
    public RatingSchemeDAO(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<RatingScheme> findAll() {
        Map<Long, Collection<RagName>> itemsByScheme = groupBy(
                d -> d.ratingSchemeId(),
                fetchItems(DSL.trueCondition()));

        return dsl
                .selectFrom(RATING_SCHEME)
                .fetch(TO_SCHEME_MAPPER)
                .stream()
                .map(s -> ImmutableRatingScheme
                        .copyOf(s)
                        .withRatings(itemsByScheme.getOrDefault(
                                s.id().get(),
                                Collections.emptyList())))
                .collect(Collectors.toList());
    }


    public RatingScheme getById(long id) {
        Condition itemCondition = RATING_SCHEME_ITEM.SCHEME_ID.eq(id);
        List<RagName> items = fetchItems(itemCondition);
        return ImmutableRatingScheme
                .copyOf(dsl
                    .selectFrom(RATING_SCHEME)
                    .where(RATING_SCHEME.ID.eq(id))
                    .fetchOne(TO_SCHEME_MAPPER))
                .withRatings(items);
    }


    public List<RagName> fetchItems(Condition itemCondition) {
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(itemCondition)
                .orderBy(RATING_SCHEME_ITEM.POSITION.asc())
                .fetch(TO_ITEM_MAPPER);
    }


    public RagName getRagNameById(long id){
        checkNotNull(id, "id cannot be null");
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.eq(id))
                .fetchOne(TO_ITEM_MAPPER);
    }


    public List<RagName> findRatingSchemeItemsForEntityAndCategory(EntityReference ref, long measurableCategoryId) {

        Condition assessmentDefinitionJoinCondition = ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID)
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id())
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name())));

        return dsl
                .select(Tables.RATING_SCHEME_ITEM.fields())
                .select(IS_RESTRICTED_FIELD)
                .from(Tables.RATING_SCHEME_ITEM)
                .innerJoin(MEASURABLE_CATEGORY).on(Tables.RATING_SCHEME_ITEM.SCHEME_ID.eq(MEASURABLE_CATEGORY.RATING_SCHEME_ID))
                .leftJoin(ASSESSMENT_DEFINITION).on(ASSESSMENT_DEFINITION.ID.eq(MEASURABLE_CATEGORY.ASSESSMENT_DEFINITION_ID))
                .leftJoin(ASSESSMENT_RATING).on(assessmentDefinitionJoinCondition)
                .leftJoin(CONSTRAINING_RATING).on(CONSTRAINING_RATING.ID.eq(ASSESSMENT_RATING.RATING_ID))
                .where(MEASURABLE_CATEGORY.ID.eq(measurableCategoryId))
                .fetch(TO_ITEM_MAPPER);
    }


    public Set<RagName> findRatingSchemeItemsByIds(Set<Long> ids) {
        checkNotNull(ids, "ids cannot be null");
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.in(ids))
                .fetchSet(TO_ITEM_MAPPER);
    }
}
