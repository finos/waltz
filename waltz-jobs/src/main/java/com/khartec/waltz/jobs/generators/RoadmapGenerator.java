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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioAxisItem;
import com.khartec.waltz.schema.tables.records.ScenarioRatingItemRecord;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.filter;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.ObjectUtilities.any;
import static com.khartec.waltz.common.RandomUtilities.randomIntBetween;
import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Roadmap.ROADMAP;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class RoadmapGenerator implements SampleDataGenerator {

    private final Set<String> roadmapNames = asSet("Test Roadmap", "Another Roadmap");


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        roadmapNames.forEach(roadmapName -> mkRoadmap(ctx, roadmapName));
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        roadmapNames.forEach(roadmapName -> removeRoadmap(ctx, roadmapName));
        return false;
    }


    // -- HELPERS ---

    private void mkRoadmap(ApplicationContext ctx, String roadmapName) {
        DSLContext dsl = getDsl(ctx);
        Long schemeId = dsl
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .fetchAny(RATING_SCHEME.ID);

        Long colTypeId = findCategoryId(dsl, "CAPABILITY");
        Long rowTypeId = findCategoryId(dsl, "PRODUCT");

        boolean anyNull = any(Objects::isNull, schemeId, colTypeId, rowTypeId);

        if (anyNull) {
            // nop
        } else {
            long roadmapId = getRoadmapDao(ctx).createRoadmap(
                    roadmapName,
                    schemeId,
                    mkRef(EntityKind.MEASURABLE_CATEGORY, colTypeId),
                    mkRef(EntityKind.MEASURABLE_CATEGORY, rowTypeId),
                    "admin");

            mkScenario(ctx, roadmapId);
        }
    }


    private void mkScenario(ApplicationContext ctx, long roadmapId) {
        ScenarioDao scenarioDao = getScenarioDao(ctx);
        RoadmapDao roadmapDao = getRoadmapDao(ctx);
        Roadmap roadmap = roadmapDao.getById(roadmapId);

        Scenario scenario = scenarioDao.add(roadmapId, "Current", "admin");
        mkAxisItems(ctx, scenario, roadmap);
        populateScenario(ctx, scenario);
    }


    private void populateScenario(ApplicationContext ctx, Scenario scenario) {
        ScenarioAxisItemDao scenarioAxisItemDao = getScenarioAxisItemDao(ctx);
        MeasurableRatingService measurableRatingService = getMeasurableRatingService(ctx);
        scenario.id().ifPresent(scenarioId -> {
            Collection<ScenarioAxisItem> axisItems = scenarioAxisItemDao.findForScenarioId(scenarioId);

            Map<AxisOrientation, Collection<ScenarioAxisItem>> byOrientation = groupBy(ScenarioAxisItem::axisOrientation, axisItems);

            IdSelectionOptions options = mkOpts(scenario.entityReference());
            Map<Long, Collection<MeasurableRating>> ratingsByMeasurableId = groupBy(
                    MeasurableRating::measurableId,
                    measurableRatingService.findByMeasurableIdSelector(options));

            List<ScenarioRatingItemRecord> scenarioRatingItems = IntStream
                    .range(0, randomIntBetween(10, 300))
                    .mapToObj(i -> tuple(
                            randomPick(byOrientation.get(AxisOrientation.COLUMN)),
                            randomPick(byOrientation.get(AxisOrientation.ROW))))
                    .map(t -> t
                            .map1(d -> d.domainItem().id())
                            .map2(d -> d.domainItem().id()))
                    .map(t -> t.concat(tuple(
                            randomPick(ratingsByMeasurableId.get(t.v1)),
                            randomPick(ratingsByMeasurableId.get(t.v2)))))
                    .map(t -> {
                        MeasurableRating rating = t.v3 != null
                                ? t.v3
                                : t.v4;

                        ScenarioRatingItemRecord record = getDsl(ctx).newRecord(SCENARIO_RATING_ITEM);
                        record.setScenarioId(scenarioId);
                        record.setColumnId(t.v1);
                        record.setColumnKind(EntityKind.MEASURABLE.name());
                        record.setRowId(t.v2);
                        record.setRowKind(EntityKind.MEASURABLE.name());
                        record.setDomainItemId(rating.entityReference().id());
                        record.setDomainItemKind(rating.entityReference().kind().name());
                        record.setRating(String.valueOf(rating.rating()));
                        record.setLastUpdatedBy("admin");
                        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                        return record;
                    })
                    .collect(Collectors.toList());

            getDsl(ctx)
                    .batchInsert(scenarioRatingItems)
                    .execute();

        });
    }


    private void mkAxisItems(ApplicationContext ctx,
                             Scenario scenario,
                             Roadmap roadmap) {
        scenario.id()
            .ifPresent(scenarioId -> {
                List<Measurable> rowCategories = pickAxisItems(ctx, roadmap.rowType(), randomIntBetween(5, 10));
                List<Measurable> colCategories = pickAxisItems(ctx, roadmap.columnType(), randomIntBetween(2, 7));
                addAxisItems(ctx, rowCategories, scenarioId, AxisOrientation.ROW);
                addAxisItems(ctx, colCategories, scenarioId, AxisOrientation.COLUMN);
            });
    }


    private void addAxisItems(ApplicationContext ctx,
                              List<Measurable> categories,
                              Long scenarioId,
                              AxisOrientation row) {
        ScenarioAxisItemDao scenarioAxisItemDao = getScenarioAxisItemDao(ctx);
        categories.forEach(c -> scenarioAxisItemDao.add(
                scenarioId,
                row, // orientation
                c.entityReference(),
                10));
    }


    private List<Measurable> pickAxisItems(ApplicationContext ctx, EntityReference type, int howMany) {
        MeasurableDao measurableDao = getMeasurableDao(ctx);

        List<Measurable> concreteChoices = filter(
                Measurable::concrete,
                measurableDao.findByCategoryId(type.id()));

        return randomPick(concreteChoices, howMany);
    }


    private void removeRoadmap(ApplicationContext ctx, String roadmapName) {
        DSLContext dsl = getDsl(ctx);

        dsl.select(ROADMAP.ID)
                .from(ROADMAP)
                .where(ROADMAP.NAME.eq(roadmapName))
                .fetchOptional(ROADMAP.ID)
                .ifPresent(rId -> {
                    SelectConditionStep<Record1<Long>> scenarioIds = DSL
                            .select(SCENARIO.ID)
                            .from(SCENARIO)
                            .where(SCENARIO.ROADMAP_ID.eq(rId));

                    dsl.deleteFrom(SCENARIO_AXIS_ITEM)
                            .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.in(scenarioIds))
                            .execute();

                    dsl.deleteFrom(SCENARIO_RATING_ITEM)
                            .where(SCENARIO_RATING_ITEM.SCENARIO_ID.in(scenarioIds))
                            .execute();

                    dsl.deleteFrom(ROADMAP)
                            .where(ROADMAP.ID.eq(rId))
                            .execute();
                });
    }


    private Long findCategoryId(DSLContext dsl, String extId) {
        return dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(extId))
                .fetchAny(MEASURABLE_CATEGORY.ID);
    }


    private ScenarioAxisItemDao getScenarioAxisItemDao(ApplicationContext ctx) {
        return ctx.getBean(ScenarioAxisItemDao.class);
    }


    private ScenarioDao getScenarioDao(ApplicationContext ctx) {
        return ctx.getBean(ScenarioDao.class);
    }


    private RoadmapDao getRoadmapDao(ApplicationContext ctx) {
        return ctx.getBean(RoadmapDao.class);
    }


    private MeasurableDao getMeasurableDao(ApplicationContext ctx) {
        return ctx.getBean(MeasurableDao.class);
    }


    private MeasurableRatingDao getMeasurableRatingDao(ApplicationContext ctx) {
        return ctx.getBean(MeasurableRatingDao.class);
    }


    private MeasurableRatingService getMeasurableRatingService(ApplicationContext ctx) {
        return ctx.getBean(MeasurableRatingService.class);
    }


    private MeasurableIdSelectorFactory getMeasurableIdSelectorFactory(ApplicationContext ctx) {
        return ctx.getBean(MeasurableIdSelectorFactory.class);
    }


}
