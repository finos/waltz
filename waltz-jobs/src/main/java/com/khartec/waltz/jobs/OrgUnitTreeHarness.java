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

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
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
