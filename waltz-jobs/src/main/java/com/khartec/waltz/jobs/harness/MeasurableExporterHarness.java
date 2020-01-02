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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.Tables.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.Person.PERSON;

public class MeasurableExporterHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        long categoryId = 1L;

        SelectConditionStep<Record> qry = dsl
                .select(MEASURABLE.NAME, MEASURABLE.DESCRIPTION, MEASURABLE.ID, MEASURABLE.PARENT_ID, MEASURABLE.EXTERNAL_ID)
                .select(INVOLVEMENT_KIND.NAME)
                .select(PERSON.DISPLAY_NAME)
                .from(MEASURABLE)
                .leftJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(MEASURABLE.ID).and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .leftJoin(INVOLVEMENT_KIND)
                .on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                .leftJoin(PERSON)
                .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId));

        qry.fetch().forEach(System.out::println);

    }
}
