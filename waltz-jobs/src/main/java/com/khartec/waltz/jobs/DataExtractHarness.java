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
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.Tables.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


public class DataExtractHarness {

    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = ctx.getBean(LogicalFlowIdSelectorFactory.class);

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.ORG_UNIT, 4326),
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> flowIdSelector = logicalFlowIdSelectorFactory.apply(options);

        Application sourceApp = APPLICATION.as("sourceApp");
        Application targetApp = APPLICATION.as("targetApp");
        OrganisationalUnit sourceOrgUnit = ORGANISATIONAL_UNIT.as("sourceOrgUnit");
        OrganisationalUnit targetOrgUnit = ORGANISATIONAL_UNIT.as("targetOrgUnit");

        Result<Record> fetch = dsl.select(LOGICAL_FLOW.fields())
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .select(sourceApp.fields())
                .select(targetApp.fields())
                .select(sourceOrgUnit.fields())
                .select(targetOrgUnit.fields())
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .from(LOGICAL_FLOW)
                .leftJoin(sourceApp).on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(sourceApp.ID).and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(targetApp).on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(targetApp.ID).and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(sourceOrgUnit).on(sourceApp.ORGANISATIONAL_UNIT_ID.eq(sourceOrgUnit.ID))
                .leftJoin(targetOrgUnit).on(targetApp.ORGANISATIONAL_UNIT_ID.eq(targetOrgUnit.ID))
                .join(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID).and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq("DATA_TYPE")))
                .where(LOGICAL_FLOW.ID.in(flowIdSelector))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                .fetch();

        System.out.printf("got records: %s", fetch.size());

    }



}
