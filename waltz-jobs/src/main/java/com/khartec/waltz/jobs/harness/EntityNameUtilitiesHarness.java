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


import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

public class EntityNameUtilitiesHarness {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("without entity names", () -> {
                dsl.selectFrom(ENTITY_STATISTIC_VALUE)
                        .fetch();
                return null;
            });
        }

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("with entity names", () -> {
                Field<String> entityNameField = InlineSelectFieldFactory.mkNameField(
                        ENTITY_STATISTIC_VALUE.ENTITY_ID,
                        ENTITY_STATISTIC_VALUE.ENTITY_KIND,
                        newArrayList(EntityKind.APPLICATION, EntityKind.ORG_UNIT));

                dsl.select(ENTITY_STATISTIC_VALUE.fields())
                        .select(entityNameField)
                        .from(ENTITY_STATISTIC_VALUE)
                        .fetch();
//                        .forEach(System.out::println);
                return null;
            });
        }
    }
}
