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

package org.finos.waltz.data.logical_flow;

import org.finos.waltz.model.EntityReference;

import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.data_type.DataTypeIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.FLOW_CLASSIFICATION_RULE;
import static org.finos.waltz.schema.Tables.INVOLVEMENT;
import static org.finos.waltz.schema.Tables.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.Tables.PERSON_HIERARCHY;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;
import static org.finos.waltz.schema.tables.TagUsage.TAG_USAGE;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static org.finos.waltz.model.HierarchyQueryScope.EXACT;


public class LogicalFlowIdSelectorFactory implements IdSelectorFactory {


    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();

    private final static Application CONSUMER_APP = APPLICATION.as("consumer");
    private final static Application SUPPLIER_APP = APPLICATION.as("supplier");



    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch (options.entityReference().kind()) {
            case ACTOR:
            case END_USER_APPLICATION:
                return mkForSpecificNode(options);
            case PERSON:
                return mkViaPersonJoinDerivedTable(options);
            case ALL:
            case APPLICATION:
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case SCENARIO:
                return wrapAppIdSelector(options);
            case DATA_TYPE:
                return mkForDataType(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            case PHYSICAL_FLOW:
                return mkForPhysicalFlow(options);
            case SERVER:
                return mkForServer(options);
            case TAG:
                return mkForTagBasedOnPhysicalFlowTags(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case FLOW_CLASSIFICATION_RULE:
                return mkForFlowClassificationRule(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: " + options);
        }
    }


    private Select<Record1<Long>> mkForSpecificNode(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        EntityReference ref = options.entityReference();

        Condition sourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()));

        Condition targetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()));

        return DSL
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(sourceCondition.or(targetCondition))
                .and(LOGICAL_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForFlowClassificationRule(IdSelectionOptions options) {
        return DSL
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .innerJoin(FLOW_CLASSIFICATION_RULE)
                .on(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID.eq(FLOW_CLASSIFICATION_RULE.ID))
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(options.entityReference().id())
                        .and(mkLifecycleStatusCondition(options)));
    }


    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an LOGICAL_DATA_FLOW ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForTagBasedOnPhysicalFlowTags(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);

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
        SelectorUtilities.ensureScopeIsExact(options);

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
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(options.entityReference().id()))
                .and(LOGICAL_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForPhysicalFlow(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .and(LOGICAL_NOT_REMOVED)
                .where(PHYSICAL_FLOW.ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);

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

    private Select<Record1<Long>> mkViaPersonJoinDerivedTable(IdSelectionOptions options) {
        SelectConditionStep<Record1<String>> employeeIdForPerson = DSL
                .select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(options.entityReference().id()));

        Table<Record3<String, String, Long>> personInvolvementDerivedTable = DSL
                .select(INVOLVEMENT.ENTITY_KIND.as("entity_kind"),
                        INVOLVEMENT.EMPLOYEE_ID.as("employee_id"),
                        APPLICATION.ID.as("app_id"))
                .from(PERSON)
                .innerJoin(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .and(PERSON_HIERARCHY.MANAGER_ID.eq(employeeIdForPerson))
                .and(PERSON.IS_REMOVED.isFalse())
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .innerJoin(INVOLVEMENT_KIND)
                .on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                .and(INVOLVEMENT_KIND.TRANSITIVE.isTrue())
                .innerJoin(APPLICATION)
                .on(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID))
                .union(
                        DSL
                                .select(INVOLVEMENT.ENTITY_KIND,
                                        INVOLVEMENT.EMPLOYEE_ID,
                                        APPLICATION.ID)
                                .from(PERSON)
                                .innerJoin(INVOLVEMENT)
                                .on(INVOLVEMENT.EMPLOYEE_ID.eq(employeeIdForPerson))
                                .innerJoin(APPLICATION)
                                .on(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                                .and(INVOLVEMENT.ENTITY_ID.eq(APPLICATION.ID))
                )
                .asTable();

        return DSL
                .selectDistinct(LOGICAL_FLOW.ID)
                .from(personInvolvementDerivedTable)
                .innerJoin(LOGICAL_FLOW)
                .on((LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(personInvolvementDerivedTable.field("entity_kind", String.class))
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(personInvolvementDerivedTable.field("app_id", Long.class))))
                    .or(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(personInvolvementDerivedTable.field("entity_kind", String.class))
                        .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(personInvolvementDerivedTable.field("app_id", Long.class)))))
                .and(LOGICAL_FLOW.IS_REMOVED.isFalse())
                .and(mkLifecycleStatusCondition(options));
    }


    private Condition mkLifecycleStatusCondition(IdSelectionOptions options) {
        return LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.in(map(options.entityLifecycleStatuses(), Enum::name));
    }

}
