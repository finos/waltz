/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.util.Collection;

import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


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
        DataSource dataSource = ctx.getBean(DataSource.class);
        InvolvementDao dao = ctx.getBean(InvolvementDao.class);


        time("hmm", () -> foo(dsl, "NravvV2Is"));

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
