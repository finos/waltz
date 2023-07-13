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

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.schema.Tables.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


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
        LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();

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
