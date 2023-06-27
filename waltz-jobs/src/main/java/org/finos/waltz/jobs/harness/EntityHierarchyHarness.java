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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.service.DIBaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Created by dwatkins on 30/07/2016.
 */
public class EntityHierarchyHarness {

    private static final long MEASURABLE_CATEGORY_ID = 9L;

    /**
     *

     select max(level) from entity_hierarchy where id in (select id from measurable where measurable_category_id = 33);




     select l1m.name, eh1.descendant_level,  l2m.name, eh2.descendant_level, l3m.name, eh3.descendant_level
     from measurable l1m
     inner join entity_hierarchy eh1 on eh1.id = l1m.id and eh1.kind = 'MEASURABLE' and eh1.descendant_level = 1



     inner join entity_hierarchy eh2 on eh2.ancestor_id = eh1.id and eh2.kind = 'MEASURABLE' and eh2.descendant_level = 2
     inner join measurable l2m on l2m.id = eh2.id and l1m.measurable_category_id = 33


     inner join entity_hierarchy eh3 on eh3.ancestor_id = eh2.id and eh3.kind = 'MEASURABLE' and eh3.descendant_level = 3
     inner join measurable l3m on l3m.id = eh3.id and l1m.measurable_category_id = 33

     * @param args
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);

        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
        Measurable m = MEASURABLE.as("m");
        MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");

        DSLContext dsl = ctx.getBean(DSLContext.class);

        Field<Integer> maxLevelField = DSL.max(eh.LEVEL).as("maxLevel");
        Integer maxLevel = dsl
                .select(maxLevelField)
                .from(eh)
                .where(eh.KIND.eq(EntityKind.MEASURABLE.name()))
                .and(eh.ID.in(DSL
                        .select(m.ID)
                        .from(m)
                        .where(m.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY_ID))))
                .fetchOne(maxLevelField);

        ArrayList<Tuple3<Integer, EntityHierarchy, Measurable>> ehAndMeasurableTablesForLevel = new ArrayList<>(maxLevel);

        for (int i = 0; i < maxLevel; i++) {
            ehAndMeasurableTablesForLevel.add(i, tuple(i + 1, ENTITY_HIERARCHY.as("eh" + i), MEASURABLE.as("l" + i)));
        }

        List<Field<?>> fields = ehAndMeasurableTablesForLevel
                .stream()
                .flatMap(t -> Stream.of(
                        _m(t).NAME.as("Level " + t.v1 + " Name"),
                        _m(t).EXTERNAL_ID.as("Level " + t.v1 + " External Id"),
                        _m(t).ID.as("Level " + t.v1 + " Waltz Id")))
                .collect(Collectors.toList());

        Tuple3<Integer, EntityHierarchy, Measurable> l1 = ehAndMeasurableTablesForLevel.get(0);

        SelectOnConditionStep<Record> baseQry = dsl
                .select(fields)
                .from(_m(l1))
                .innerJoin(_eh(l1))
                .on(_eh(l1).ID.eq(_m(l1).ID)
                        .and(_eh(l1).KIND.eq(EntityKind.MEASURABLE.name()))
                        .and(_eh(l1).DESCENDANT_LEVEL.eq(_lvl(l1)))
                        .and(_m(l1).MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY_ID)));

        for (int i = 1; i < maxLevel; i++) {
            Tuple3<Integer, EntityHierarchy, Measurable> curr = ehAndMeasurableTablesForLevel.get(i);
            Tuple3<Integer, EntityHierarchy, Measurable> prev = ehAndMeasurableTablesForLevel.get(i - 1);
            baseQry = baseQry
                    .leftJoin(_eh(curr))
                    .on(_eh(curr).ANCESTOR_ID.eq(_eh(prev).ID)
                            .and(_eh(curr).KIND.eq(EntityKind.MEASURABLE.name())
                                    .and(_eh(curr).DESCENDANT_LEVEL.eq(_lvl(curr)))))
                    .leftJoin(_m(curr))
                    .on(_m(curr).ID.eq(_eh(curr).ID));
        }

        System.out.println(baseQry);

    }

    private static Measurable _m(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v3;
    }

    private static EntityHierarchy _eh(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v2;
    }

    private static Integer _lvl(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v1;
    }
}
