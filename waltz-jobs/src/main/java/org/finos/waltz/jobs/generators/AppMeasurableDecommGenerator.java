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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingReplacementRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.common.DateTimeUtilities.today;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.RandomUtilities.pickAndRemove;
import static org.finos.waltz.common.RandomUtilities.randomIntBetween;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.RandomUtilities.randomTrue;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.SetUtilities.minus;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_REPLACEMENT;


public class AppMeasurableDecommGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);

        MeasurableCategoryDao categoryDao = ctx.getBean(MeasurableCategoryDao.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);
        MeasurableRatingDao ratingDao = ctx.getBean(MeasurableRatingDao.class);

        first(categoryDao.findByExternalId("CAPABILITY"))
                .id()
                .ifPresent(categoryId -> {
                    Collection<MeasurableRating> ratings = ratingDao.findByCategory(categoryId);

                    int[] rc = ratings
                            .stream()
                            .filter(e -> randomTrue(0.1))
                            .map(r -> {
                                MeasurableRatingPlannedDecommissionRecord record = dsl.newRecord(MEASURABLE_RATING_PLANNED_DECOMMISSION);
                                record.setMeasurableRatingId(r.id().orElseThrow(() -> new IllegalStateException("Read a rating record with no id ?!")));
                                record.setPlannedDecommissionDate(mkFutureDate());

                                record.setCreatedBy("test");
                                record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
                                record.setUpdatedBy("test");
                                record.setUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                                return record;
                            })
                            .collect(collectingAndThen(
                                    Collectors.toSet(),
                                    dsl::batchInsert))
                            .execute();


                    List<Application> allApps = applicationDao.findAll();
                    Map<Long, Collection<MeasurableRating>> allRatingsByMeasurableId = groupBy(
                            ratings,
                            MeasurableRating::measurableId);

                    dsl
                            .select()
                            .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                            .innerJoin(MEASURABLE_RATING).on(MEASURABLE_RATING.ID.eq(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_RATING_ID))
                            .fetchGroups(r -> r.get(MEASURABLE_RATING.MEASURABLE_ID))
                            .entrySet()
                            .stream()
                            .flatMap(e -> {
                                Set<Long> appIdsForMeasurable = map(
                                        allRatingsByMeasurableId.get(e.getValue()),
                                        r -> r.entityReference().id());

                                Set<Long> decommingAppIds = map(
                                        e.getValue(),
                                        r -> r.get(MEASURABLE_RATING.ENTITY_ID));

                                Set<Long> possibleReplacements = minus(
                                        appIdsForMeasurable,
                                        decommingAppIds);

                                Set<Long> replacementAppIds = new HashSet<>();
                                if (possibleReplacements.size() > 1) {
                                    Tuple2<Long, List<Long>> firstPick = pickAndRemove(new ArrayList<>(possibleReplacements));
                                    Tuple2<Long, List<Long>> secondPick = pickAndRemove(firstPick.v2);
                                    replacementAppIds.add(firstPick.v1);
                                    replacementAppIds.add(secondPick.v1);
                                }
                                replacementAppIds.add(randomPick(allApps).id().get());


                                return e.getValue()
                                        .stream()
                                        .filter(r -> randomTrue(0.7))
                                        .map(r -> {
                                            MeasurableRatingReplacementRecord record = dsl.newRecord(MEASURABLE_RATING_REPLACEMENT);
                                            record.setDecommissionId(r.get(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID));
                                            record.setEntityId(randomPick(replacementAppIds));
                                            record.setEntityKind(EntityKind.APPLICATION.name());
                                            record.setPlannedCommissionDate(mkFutureDate());

                                            record.setCreatedBy("test");
                                            record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
                                            record.setUpdatedBy("test");
                                            record.setUpdatedAt(DateTimeUtilities.nowUtcTimestamp());

                                            return record;
                                        });
                            })
                            .collect(collectingAndThen(Collectors.toSet(), dsl::batchInsert))
                            .execute();

                });
        log("hello %s", "world");
        return null;
    }

    private Date mkFutureDate() {
        return toSqlDate(today().plusDays(randomIntBetween(100, 600)));
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .where(MEASURABLE_RATING_PLANNED_DECOMMISSION.CREATED_BY.eq("test"))
                .execute();
        return true;
    }



    public static void main(String[] args) {
        LoggingUtilities.configureLogging();

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        new AppMeasurableDecommGenerator().create(ctx);
    }


}
