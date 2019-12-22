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
