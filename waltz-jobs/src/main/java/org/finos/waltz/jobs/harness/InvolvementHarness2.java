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

import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.data.involvement_kind.InvolvementKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.Set;

import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


public class InvolvementHarness2 {

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
        InvolvementKindDao dao = ctx.getBean(InvolvementKindDao.class);
        InvolvementDao invDao = ctx.getBean(InvolvementDao.class);

//        InvolvementKindUsageStat stats = dao.loadUsageStatsForKind(14L);

        Set<Involvement> invs = invDao.findByKindIdAndEntityKind(17L, EntityKind.APPLICATION);

        System.out.println("done");
//        time("findAllApps", () -> dao.findAllApplicationsByEmployeeId("NravvV2Is"));





    }


    public static Collection<Application> foo(DSLContext dsl, String employeeId) {
        SelectOrderByStep<Record1<String>> employeeIds = dsl
                .selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                .union(DSL.select(DSL.value(employeeId)).from(PERSON_HIERARCHY));

        SelectConditionStep<Record1<Long>> applicationIds = dsl
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND
                        .eq(EntityKind.APPLICATION.name())
                        .and(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));

        SelectConditionStep<Record> query = dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .where(APPLICATION.ID.in(applicationIds));

        System.out.println(query);

        return query
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }



    public static Result<Record1<String>> foo2(DSLContext dsl, String employeeId) {
        SelectOrderByStep<Record1<String>> employeeIds = dsl
                .selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                .union(DSL
                        .select(DSL.value(employeeId))
                        .from(PERSON_HIERARCHY));

        System.out.println("----SQL\n" + employeeIds);
        return employeeIds.fetch();
    }



}
