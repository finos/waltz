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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.involvement.InvolvementService;
import org.jooq.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static org.jooq.impl.DSL.select;


public class InvolvementHarness {

    private static String qry = "select \n" +
            "  [application].[name] \n" +
            "from [application]\n" +
            "where [application].[id] in (\n" +
            "  select [involvement].[entity_id]\n" +
            "  from [involvement]\n" +
            "  where (\n" +
            "    (\n" +
            "      [involvement].[employee_id] = 'Ms6tJhlJn'\n" +
            "      or [involvement].[employee_id] in (\n" +
            "        select [person_hierarchy].[employee_id]\n" +
            "        from [person_hierarchy]\n" +
            "        where [person_hierarchy].[manager_id] = 'Ms6tJhlJn'\n" +
            "      )\n" +
            "    )\n" +
            "    and [involvement].[entity_kind] = 'APPLICATION'\n" +
            "  )\n" +
            ")";

    public static void main(String[] args) throws InterruptedException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataSource dataSource = ctx.getBean(DataSource.class);
        InvolvementDao dao = ctx.getBean(InvolvementDao.class);
        InvolvementService service = ctx.getBean(InvolvementService.class);

        System.out.println("-- Waiting ...");
//        Thread.sleep(5000);

        System.out.println("-- Starting ...");
//        viaJdbc(dataSource);
//        viaJooqSql(dsl);
//        viaJooqOrig(dsl);
//        viaJooqJoins(dsl);
//        viaJooqSql(dsl);
//        viaDao(dao);
//        viaJdbc(dataSource);

    }


    private static void viaJdbc(DataSource dataSource) {

        try (
            Connection conn = dataSource.getConnection()
        ){

            System.out.println("-- jdbc start");
            long start = System.currentTimeMillis();

            PreparedStatement pstmt = conn.prepareStatement(qry);
            ResultSet rs = pstmt.executeQuery();

            int c = 0;

            while(rs.next()) {
                c++;
            }

            System.out.println(c);
            long duration = System.currentTimeMillis() - start;
            System.out.println("-- jdbc end "+ duration);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viaJooqSql(DSLContext dsl) {
        System.out.println("-- jooq sql start");
        long st = System.currentTimeMillis();

        dsl.execute(qry);

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq sql end " + dur);

    }

    private static void viaJooqJoins(DSLContext dsl) {

        String employeeId = "Ms6tJhlJn";

        Condition eitherPersonOrReportee = PERSON_HIERARCHY.MANAGER_ID.eq(employeeId)
                .or(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId));

        SelectConditionStep<Record1<Long>> query = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .innerJoin(INVOLVEMENT)
                    .on(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID))
                .innerJoin(PERSON_HIERARCHY)
                    .on(PERSON_HIERARCHY.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                    .and(eitherPersonOrReportee));

        System.out.println("-- jooq join start");
        long st = System.currentTimeMillis();

        System.out.println(query);

        Result<Record1<Long>> records = query.fetch();
        System.out.println(records.size());

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq join end " + dur);
    }

    private static void viaJooqOrig(DSLContext dsl) {

        String employeeId = "Ms6tJhlJn";

        SelectConditionStep<Record1<String>> allReporteesQuery = select(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId));

        SelectConditionStep<Record1<Long>> appIdsQuery = select(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)
                        .or(INVOLVEMENT.EMPLOYEE_ID.in(allReporteesQuery))
                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

        SelectConditionStep<Record1<String>> query = dsl.select(APPLICATION.NAME)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appIdsQuery));


        System.out.println("-- jooq orig start");
        long st = System.currentTimeMillis();

        query.fetch();

        long dur = System.currentTimeMillis() - st;
        System.out.println("-- jooq orig end " + dur);
    }

}
