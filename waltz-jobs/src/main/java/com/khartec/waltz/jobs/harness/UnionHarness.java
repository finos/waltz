/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.Tables.APPLICATION;


public class UnionHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        SelectConditionStep<Record1<Long>> part1 = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.KIND.eq(ApplicationKind.IN_HOUSE.name()));

        SelectConditionStep<Record1<Long>> part2 = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.KIND.eq(ApplicationKind.IN_HOUSE.name()));

        SelectJoinStep<Record1<Long>> t = DSL
                .select(DSL.field("id", Long.class))
                .from(part1.unionAll(part2));

        System.out.println(t);

        SelectConditionStep<Record1<String>> q = dsl
                .select(APPLICATION.NAME)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(t));

        System.out.println(q);
        System.out.println(q.fetch(APPLICATION.NAME));

    }

}
