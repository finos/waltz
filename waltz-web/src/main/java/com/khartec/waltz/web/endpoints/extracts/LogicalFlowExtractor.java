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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.InlineSelectFieldFactory.mkNameField;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class LogicalFlowExtractor extends DirectQueryBasedDataExtractor {

    private static final Field<String> SOURCE_NAME_FIELD = mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private static final Field<String> TARGET_NAME_FIELD = mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();

    @Autowired
    public LogicalFlowExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "logical-flows"), (request, response) -> {
            IdSelectionOptions options = readIdSelectionOptionsFromBody(request);
            SelectConditionStep<Record> qry = prepareQuery(dsl, options);
            return writeExtract("logical-flows", qry, request, response);
        });
    }


    private SelectConditionStep<Record> prepareQuery(DSLContext dsl, IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);

        Condition conditionForDataType = EntityKind.DATA_TYPE.equals(options.entityReference().kind())
                ? LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeIdSelectorFactory.apply(options))
                : DSL.trueCondition();

        Field<Long> sourceFlowId = LOGICAL_FLOW.ID.as("sourceFlowId");
        Field<Long> targetFlowId = LOGICAL_FLOW.ID.as("targetFlowId");

        Select<Record1<Long>> sourceAppFlows = DSL.select(sourceFlowId)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                    .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(APPLICATION.ID))
                .where(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(APPLICATION.ID.in(appIdSelector));

        Select<Record1<Long>> targetAppFlows = DSL.select(targetFlowId)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                    .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(APPLICATION.ID))
                .where(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(APPLICATION.ID.in(appIdSelector));

        Field<String> sourceAssetCodeField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        Field<String> sourceOrgUnitNameField = DSL
                .when(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(ORGANISATIONAL_UNIT.NAME)
                                .from(APPLICATION)
                                .innerJoin(ORGANISATIONAL_UNIT)
                                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)));

        Field<String> targetAssetCodeField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(APPLICATION.ASSET_CODE)
                                .from(APPLICATION)
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        Field<String> targetOrgUnitNameField = DSL
                .when(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()),
                        DSL.select(ORGANISATIONAL_UNIT.NAME)
                                .from(APPLICATION)
                                .innerJoin(ORGANISATIONAL_UNIT)
                                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                                .where(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        return dsl
                .select(SOURCE_NAME_FIELD.as("Source"),
                        sourceAssetCodeField.as("Source Asset Code"),
                        sourceOrgUnitNameField.as("Source Org Unit"))
                .select(TARGET_NAME_FIELD.as("Target"),
                        targetAssetCodeField.as("Target Asset Code"),
                        targetOrgUnitNameField.as("Target Org Unit"))
                .select(DATA_TYPE.NAME.as("Data Type"))
                .select(ENUM_VALUE.DISPLAY_NAME.as("Authoritativeness"))
                .from(LOGICAL_FLOW)
                .leftJoin(sourceAppFlows)
                    .on(sourceFlowId.eq(LOGICAL_FLOW.ID))
                .leftJoin(targetAppFlows)
                    .on(targetFlowId.eq(LOGICAL_FLOW.ID))
                .join(LOGICAL_FLOW_DECORATOR)
                    .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .join(DATA_TYPE)
                    .on(DATA_TYPE.ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .join(ENUM_VALUE)
                    .on(ENUM_VALUE.KEY.eq(LOGICAL_FLOW_DECORATOR.RATING)
                            .and(ENUM_VALUE.TYPE.eq("AuthoritativenessRating")))
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                .and(conditionForDataType)
                .and(sourceFlowId.isNotNull()
                        .or(targetFlowId.isNotNull()));

    }

}
