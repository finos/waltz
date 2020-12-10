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
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.schema.tables.records.MeasurableRatingPlannedDecommissionRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRatingReplacementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.DateTimeUtilities.today;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.RandomUtilities.*;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.common.SetUtilities.minus;
import static com.khartec.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static com.khartec.waltz.schema.Tables.MEASURABLE_RATING_REPLACEMENT;
import static java.util.stream.Collectors.collectingAndThen;


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
                                record.setEntityId(r.entityReference().id());
                                record.setEntityKind(r.entityReference().kind().name());
                                record.setMeasurableId(r.measurableId());
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
                            .selectFrom(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                            .fetchGroups(r -> r.get(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID))
                            .entrySet()
                            .stream()
                            .flatMap(e -> {
                                Set<Long> appIdsForMeasurable = map(
                                        allRatingsByMeasurableId.get(e.getValue()),
                                        r -> r.entityReference().id());

                                Set<Long> decommingAppIds = map(
                                        e.getValue(),
                                        MeasurableRatingPlannedDecommissionRecord::getEntityId);

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
                                            record.setDecommissionId(r.getId());
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
