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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.rating.*;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.records.RatingSchemeItemRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.RatingScheme.RATING_SCHEME;
import static com.khartec.waltz.schema.tables.RatingSchemeItem.RATING_SCHEME_ITEM;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Repository
public class RatingSchemeDAO {

    public static final com.khartec.waltz.schema.tables.RatingSchemeItem CONSTRAINING_RATING = Tables.RATING_SCHEME_ITEM.as("constrainingRating");

    public static final Field<Boolean> IS_RESTRICTED_FIELD = DSL.coalesce(
            DSL.field(Tables.RATING_SCHEME_ITEM.POSITION.lt(CONSTRAINING_RATING.POSITION)), false)
            .as("isRestricted");



    public static final RecordMapper<Record, RatingSchemeItem> TO_ITEM_MAPPER = record -> {

        RatingSchemeItemRecord r = record.into(RATING_SCHEME_ITEM);

        ImmutableRatingSchemeItem.Builder builder = ImmutableRatingSchemeItem.builder()
                .id(r.getId())
                .ratingSchemeId(r.getSchemeId())
                .name(r.getName())
                .rating(firstChar(r.getCode(), 'X'))
                .userSelectable(r.getUserSelectable())
                .color(r.getColor())
                .position(r.getPosition())
                .description(r.getDescription())
                .externalId(ofNullable(r.getExternalId()));


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
        Map<Optional<Long>, Collection<RatingSchemeItem>> itemsByScheme = groupBy(
                rsi -> Optional.of(rsi.ratingSchemeId()),
                fetchItems(DSL.trueCondition()));

        return dsl
                .selectFrom(RATING_SCHEME)
                .fetch(TO_SCHEME_MAPPER)
                .stream()
                .map(s -> ImmutableRatingScheme
                        .copyOf(s)
                        .withRatings(itemsByScheme.getOrDefault(
                                s.id(),
                                emptyList())))
                .collect(toList());
    }


    public RatingScheme getById(long id) {
        Condition itemCondition = RATING_SCHEME_ITEM.SCHEME_ID.eq(id);
        List<RatingSchemeItem> items = fetchItems(itemCondition);
        return ImmutableRatingScheme
                .copyOf(dsl
                    .selectFrom(RATING_SCHEME)
                    .where(RATING_SCHEME.ID.eq(id))
                    .fetchOne(TO_SCHEME_MAPPER))
                .withRatings(items);
    }


    public List<RatingSchemeItem> fetchItems(Condition itemCondition) {
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(itemCondition)
                .orderBy(RATING_SCHEME_ITEM.POSITION.asc())
                .fetch(TO_ITEM_MAPPER);
    }

    public List<RatingSchemeItem> findRatingSchemeItemsForAssessmentDefinition(Long assessmentDefinitionId) {
        return dsl
                .select(RATING_SCHEME_ITEM.fields())
                .from(RATING_SCHEME_ITEM)
                .innerJoin(RATING_SCHEME)
                .on(RATING_SCHEME.ID.eq(RATING_SCHEME_ITEM.SCHEME_ID))
                .innerJoin(ASSESSMENT_DEFINITION)
                .on(ASSESSMENT_DEFINITION.RATING_SCHEME_ID.eq(RATING_SCHEME.ID))
                .where(ASSESSMENT_DEFINITION.ID.eq(assessmentDefinitionId))
                .orderBy(RATING_SCHEME_ITEM.POSITION.asc())
                .fetch(TO_ITEM_MAPPER);
    }

    public RatingSchemeItem getRatingSchemeItemById(long id){
        checkNotNull(id, "id cannot be null");
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.eq(id))
                .fetchOne(TO_ITEM_MAPPER);
    }


    public List<RatingSchemeItem> findRatingSchemeItemsForEntityAndCategory(EntityReference ref, long measurableCategoryId) {

        Condition assessmentDefinitionJoinCondition = ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID)
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id())
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name())));

        return dsl
                .select(Tables.RATING_SCHEME_ITEM.fields())
                .select(IS_RESTRICTED_FIELD)
                .from(Tables.RATING_SCHEME_ITEM)
                .innerJoin(MEASURABLE_CATEGORY).on(Tables.RATING_SCHEME_ITEM.SCHEME_ID.eq(MEASURABLE_CATEGORY.RATING_SCHEME_ID))
                .leftJoin(ASSESSMENT_DEFINITION).on(ASSESSMENT_DEFINITION.ID.eq(MEASURABLE_CATEGORY.CONSTRAINING_ASSESSMENT_DEFINITION_ID))
                .leftJoin(ASSESSMENT_RATING).on(assessmentDefinitionJoinCondition)
                .leftJoin(CONSTRAINING_RATING).on(CONSTRAINING_RATING.ID.eq(ASSESSMENT_RATING.RATING_ID))
                .where(MEASURABLE_CATEGORY.ID.eq(measurableCategoryId))
                .fetch(TO_ITEM_MAPPER);
    }


    public Set<RatingSchemeItem> findRatingSchemeItemsByIds(Set<Long> ids) {
        checkNotNull(ids, "ids cannot be null");
        return dsl
                .selectFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.in(ids))
                .fetchSet(TO_ITEM_MAPPER);
    }


    public Boolean save(RatingScheme scheme) {
        RatingSchemeRecord r = dsl.newRecord(RATING_SCHEME);
        r.setName(scheme.name());
        r.setDescription(scheme.description());

        return scheme.id()
            .map(id -> {
                r.setId(id);
                r.changed(RATING_SCHEME.ID, false);
                return r.update() == 1;
            })
            .orElseGet(() -> r.insert() == 1);
    }


    public Boolean saveRatingItem(long schemeId,
                                  RatingSchemeItem item) {
        RatingSchemeItemRecord r = dsl.newRecord(RATING_SCHEME_ITEM);

        r.setSchemeId(schemeId);
        r.setName(item.name());
        r.setDescription(item.description());
        r.setCode(Character.toString(item.rating()));
        r.setColor(item.color());
        r.setPosition(item.position());
        r.setUserSelectable(item.userSelectable());

        return item.id()
                .map(id -> {
                    r.setId(id);
                    r.changed(RATING_SCHEME_ITEM.ID, false);
                    return r.update() == 1;
                })
                .orElseGet(() -> r.insert() == 1);
    }


    public Boolean removeRatingItem(long itemId) {
        return dsl
                .deleteFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.eq(itemId))
                .execute() == 1;
    }


    public Boolean removeRatingScheme(long id) {
        return dsl
            .transactionResult(ctx -> {
                DSLContext tx = ctx.dsl();
                tx.deleteFrom(RATING_SCHEME_ITEM)
                        .where(RATING_SCHEME_ITEM.SCHEME_ID.eq(id))
                        .execute();
                return tx.deleteFrom(RATING_SCHEME)
                        .where(RATING_SCHEME.ID.eq(id))
                        .execute() == 1;
            });
    }


    public List<RatingSchemeItemUsageCount> calcRatingUsageStats() {

        com.khartec.waltz.schema.tables.RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
        com.khartec.waltz.schema.tables.RatingScheme rs = RATING_SCHEME.as("rs");
        com.khartec.waltz.schema.tables.AssessmentRating ar = ASSESSMENT_RATING.as("ar");
        com.khartec.waltz.schema.tables.MeasurableRating mr = MEASURABLE_RATING.as("mr");
        com.khartec.waltz.schema.tables.Measurable m = MEASURABLE.as("m");
        com.khartec.waltz.schema.tables.MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
        com.khartec.waltz.schema.tables.ScenarioRatingItem sri = SCENARIO_RATING_ITEM.as("sri");
        com.khartec.waltz.schema.tables.Scenario s = SCENARIO.as("s");
        com.khartec.waltz.schema.tables.Roadmap r = ROADMAP.as("r");

        SelectHavingStep<Record4<Long, Long, String, Integer>> assessmentCounts = dsl
                .select(rsi.SCHEME_ID, rsi.ID, DSL.val("ASSESSMENT_RATING"), DSL.count())
                .from(ar)
                .innerJoin(rsi).on(rsi.ID.eq(ar.RATING_ID))
                .groupBy(rsi.ID);

        SelectHavingStep<Record4<Long, Long, String, Integer>> measurableCounts = dsl
                .select(rsi.SCHEME_ID, rsi.ID, DSL.val("MEASURABLE_RATING"), DSL.count())
                .from(mr)
                .innerJoin(m).on(m.ID.eq(mr.MEASURABLE_ID))
                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                .innerJoin(rs).on(rs.ID.eq(mc.RATING_SCHEME_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(mr.RATING)).and(rsi.SCHEME_ID.eq(rs.ID))
                .groupBy(rsi.ID);

        SelectHavingStep<Record4<Long, Long, String, Integer>> scenarioCounts = dsl
                .select(rsi.SCHEME_ID, rsi.ID, DSL.val("SCENARIO"), DSL.count())
                .from(sri)
                .innerJoin(s).on(s.ID.eq(sri.SCENARIO_ID))
                .innerJoin(r).on(r.ID.eq(s.ROADMAP_ID))
                .innerJoin(rs).on(rs.ID.eq(r.RATING_SCHEME_ID))
                .innerJoin(rsi).on(rsi.CODE.eq(sri.RATING)).and(rsi.SCHEME_ID.eq(rs.ID))
                .groupBy(rsi.ID);

        return assessmentCounts
                .union(measurableCounts)
                .union(scenarioCounts)
                .fetch(res -> ImmutableRatingSchemeItemUsageCount
                        .builder()
                        .schemeId(res.get(0, Long.class))
                        .ratingId(res.get(1, Long.class))
                        .usageKind(EntityKind.valueOf(res.get(2, String.class)))
                        .count(res.get(3, Integer.class))
                        .build());
    }


}
