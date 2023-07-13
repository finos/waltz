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

import org.finos.waltz.common.Checks;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.jooq.impl.DSL.*;

/**
 * Created by dwatkins on 29/07/2016.
 */
public class OrgUnitTreeHarness {

    private static final OrgUnitTreeHarness ouHierarchyHelper = new OrgUnitTreeHarness(
            ORGANISATIONAL_UNIT,
            ORGANISATIONAL_UNIT.ID,
            ORGANISATIONAL_UNIT.PARENT_ID);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);


        CommonTableExpression<Record2<Long, Long>> ancestors = ouHierarchyHelper.findAncestors(50L);
        CommonTableExpression<Record2<Long, Long>> descendents = ouHierarchyHelper.findDescendents(50L);
        CommonTableExpression<Record2<Long, Long>> provided = name("treeTable").fields("id", "parent_id")
                .as(select(val(150L, Long.class), val(150L, Long.class)));


        CommonTableExpression cte = provided;

        SelectOnConditionStep<Record1<Long>> appIdSelector = dsl.with(cte)
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .innerJoin(cte)
                .on(ancestors.field("id", Long.class).eq(APPLICATION.ORGANISATIONAL_UNIT_ID));

        dsl.select(APPLICATION.NAME).from(APPLICATION).where(APPLICATION.ID.in(appIdSelector)).forEach(System.out::println);


//        dsl.selectFrom(ouHierarchyHelper.findAncestors(50L).asTable())
//                .forEach(System.out::println);

    }


    private static final Table<Record> treeTable = table(name("treeTable"));
    private static final Field<Long> treeIdField = field(name("treeTable", "id"), Long.class);
    private static final Field<Long> treeParentField = field(name("treeTable", "parent_id"), Long.class);

    private final Table table;
    private final Field<Long> idField;
    private final Field<Long> parentIdField;


    public OrgUnitTreeHarness(Table table, Field<Long> idField, Field<Long> parentIdField) {
        Checks.checkNotNull(table, "table cannot be null");
        Checks.checkNotNull(idField, "idField cannot be null");
        Checks.checkNotNull(parentIdField, "parentIdField cannot be null");

        this.table = table;
        this.idField = idField;
        this.parentIdField = parentIdField;
    }

    public CommonTableExpression<Record2<Long, Long>> findAncestors(long id) {

        SelectConditionStep<Record2<Long, Long>> recursiveStep = select(idField, parentIdField)
                .from(table, treeTable)
                .where(idField.eq(treeParentField));

        return findRecursively(id, recursiveStep);
    }




    public CommonTableExpression<Record2<Long, Long>> findDescendents(long id) {

        SelectConditionStep<Record2<Long, Long>> recursiveStep = select(idField, parentIdField)
                .from(table, treeTable)
                .where(parentIdField.eq(treeIdField));

        return findRecursively(id, recursiveStep);
    }


    private CommonTableExpression<Record2<Long, Long>> findRecursively(long id, SelectConditionStep<Record2<Long, Long>> recursiveStep) {

        CommonTableExpression<Record2<Long, Long>> cte = name("treeTable").fields("id", "parent_id")
                .as(select(idField, parentIdField)
                        .from(table)
                        .where(idField.eq(id))
                        .unionAll(recursiveStep));

        return cte;


    }
}
