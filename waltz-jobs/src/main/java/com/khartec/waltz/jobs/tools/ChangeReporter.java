/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
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

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;

public class ChangeReporter {

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        ApplicationIdSelectorFactory selectorFactory = ctx.getBean(ApplicationIdSelectorFactory.class);

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