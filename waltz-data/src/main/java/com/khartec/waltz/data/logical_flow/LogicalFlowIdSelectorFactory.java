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

package com.khartec.waltz.data.logical_flow;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;
import static com.khartec.waltz.schema.tables.TagUsage.TAG_USAGE;


public class LogicalFlowIdSelectorFactory implements IdSelectorFactory {


    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();

    private final static Application CONSUMER_APP = APPLICATION.as("consumer");
    private final static Application SUPPLIER_APP = APPLICATION.as("supplier");



    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch (options.entityReference().kind()) {
            case APPLICATION:
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case SCENARIO:
                return wrapAppIdSelector(options);
            case DATA_TYPE:
                return mkForDataType(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            case SERVER:
                return mkForServer(options);
            case TAG:
                return mkForTagBasedOnPhysicalFlowTags(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: " + options);
        }
    }

    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an LOGICAL_DATA_FLOW ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }

    private Select<Record1<Long>> mkForTagBasedOnPhysicalFlowTags(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        return DSL
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .innerJoin(TAG_USAGE)
                .on(TAG_USAGE.ENTITY_ID.eq(PHYSICAL_FLOW.ID)
                        .and(TAG_USAGE.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .where(TAG_USAGE.TAG_ID.eq(options.entityReference().id()))
                .and(mkLifecycleStatusCondition(options));
    }


    private Select<Record1<Long>> mkForServer(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition lifecycleCondition = (options.entityLifecycleStatuses().contains(EntityLifecycleStatus.REMOVED)
                    ? DSL.trueCondition()
                    : PHYSICAL_FLOW.IS_REMOVED.isFalse())
                .and(mkLifecycleStatusCondition(options));

        long serverId = options.entityReference().id();
        return DSL
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .innerJoin(PHYSICAL_FLOW_PARTICIPANT)
                .on(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID.eq(PHYSICAL_FLOW.ID))
                .where(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND.eq(EntityKind.SERVER.name()))
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID.eq(serverId))
                .and(lifecycleCondition);
    }


    private Select<Record1<Long>> mkForPhysicalSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(options.entityReference().id()))
                .and(LOGICAL_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(LOGICAL_FLOW.ID))
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.LOGICAL_DATA_FLOW.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(options.entityReference().id()))
                .and(mkLifecycleStatusCondition(options));
    }



    private Select<Record1<Long>> wrapAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);

        Condition sourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition targetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return DSL
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(sourceCondition.or(targetCondition))
                .and(LOGICAL_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForApplication(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long appId = options.entityReference().id();
        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(appId)
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(appId)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .and(LOGICAL_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForDataType(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeSelector = dataTypeIdSelectorFactory.apply(options);

        Condition supplierNotRemoved =  SUPPLIER_APP.IS_REMOVED.isFalse();
        Condition consumerNotRemoved =  CONSUMER_APP.IS_REMOVED.isFalse();

        Condition appKindFilterConditions = options.filters().omitApplicationKinds().isEmpty()
                ? DSL.trueCondition()
                : SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds())
                    .or(CONSUMER_APP.KIND.notIn(options.filters().omitApplicationKinds()));

        return DSL.select(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID)
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .innerJoin(SUPPLIER_APP)
                    .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(SUPPLIER_APP.ID)
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .innerJoin(CONSUMER_APP)
                    .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(CONSUMER_APP.ID)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeSelector)
                    .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .and(LOGICAL_NOT_REMOVED)
                .and(supplierNotRemoved)
                .and(consumerNotRemoved)
                .and(appKindFilterConditions);
    }


    private Condition mkLifecycleStatusCondition(IdSelectionOptions options) {
        return LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.in(map(options.entityLifecycleStatuses(), Enum::name));
    }

}
