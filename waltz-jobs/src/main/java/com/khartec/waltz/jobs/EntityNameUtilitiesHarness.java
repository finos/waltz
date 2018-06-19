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
