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

package org.finos.waltz.jobs.tools;

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG;

public class ChangeReporter {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        ApplicationIdSelectorFactory selectorFactory = new ApplicationIdSelectorFactory();

        LocalDateTime exerciseStart = LocalDateTime.of(2018, 06, 04, 0, 1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime dayBeforeYesterday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1);
        LocalDateTime yesterday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        EntityReference appGroupRef = EntityReference.mkRef(EntityKind.APP_GROUP, 10862); // BCBS239
        Select<Record1<Long>> appSelector = mkSelector(selectorFactory, appGroupRef);

        Tuple3<String, LocalDateTime, LocalDateTime> dayPeriod = Tuple.tuple("day", dayBeforeYesterday, yesterday);
        Tuple3<String, LocalDateTime, LocalDateTime> cumulativePeriod = Tuple.tuple("cumulative", exerciseStart, yesterday);

        dumpReport(dsl, dayPeriod, appGroupRef, appSelector);
        dumpReport(dsl, cumulativePeriod, appGroupRef, appSelector);
    }

    private static String dumpReport(DSLContext dsl,
                                     Tuple3<String, LocalDateTime, LocalDateTime> period,
                                     EntityReference appGroupRef,
                                     Select<Record1<Long>> appSelector) throws IOException {
        String filename = mkFilename(period, appGroupRef);
        String fqn = "c:/temp/" + filename;
        FileWriter writer = new FileWriter(fqn, false);
        summarizeChangesByAppSelector(dsl, appSelector, period.v2, period.v3)
                .formatCSV(writer, ',');

        System.out.println("Done: " + period.v1 + " -> " + fqn);
        return fqn;
    }


    private static String mkFilename(Tuple3<String, LocalDateTime, LocalDateTime> period, EntityReference appGroupRef) {
        return StringUtilities.join(
                        newArrayList(
                                period.v1,
                                "activity_report",
                                appGroupRef.id(),
                                "from",
                                period.v2.format(DateTimeFormatter.ISO_DATE),
                                "to",
                                period.v3.format(DateTimeFormatter.ISO_DATE)),
                        "_") + ".csv";
    }


    private static Select<Record1<Long>> mkSelector(ApplicationIdSelectorFactory selectorFactory, EntityReference appGroupRef) {
        IdSelectionOptions idSelectionOptions = IdSelectionOptions.mkOpts(appGroupRef, HierarchyQueryScope.EXACT);
        return selectorFactory.apply(idSelectionOptions);
    }


    private static Result<Record> summarizeChangesByAppSelector(
            DSLContext dsl,
            Select<Record1<Long>> appIdSelector,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        Field<Integer> countField = DSL.count();

        return dsl
                .select(APPLICATION.NAME.as("Application Name"),
                        CHANGE_LOG.PARENT_ID.as("Application ID"),
                        CHANGE_LOG.USER_ID.as("User ID"),
                        CHANGE_LOG.CHILD_KIND.as("Updated Entity Type"),
                        CHANGE_LOG.OPERATION.as("Operation"))
                .select(countField.as("Count"))
                .from(CHANGE_LOG)
                .join(APPLICATION)
                .on(CHANGE_LOG.PARENT_ID.eq(APPLICATION.ID)
                        .and(CHANGE_LOG.PARENT_KIND.eq(EntityKind.APPLICATION.name())))
                .where(CHANGE_LOG.PARENT_KIND.eq(EntityKind.APPLICATION.name())
                        .and(CHANGE_LOG.PARENT_ID.in(appIdSelector))
                        .and(CHANGE_LOG.CREATED_AT.between(Timestamp.valueOf(startTime), Timestamp.valueOf(endTime))))
                .groupBy(CHANGE_LOG.PARENT_ID,
                        APPLICATION.NAME,
                        CHANGE_LOG.USER_ID,
                        CHANGE_LOG.CHILD_KIND,
                        CHANGE_LOG.OPERATION)
                .orderBy(APPLICATION.NAME,
                        CHANGE_LOG.USER_ID,
                        CHANGE_LOG.CHILD_KIND,
                        CHANGE_LOG.OPERATION)
                .fetch();
    }
}