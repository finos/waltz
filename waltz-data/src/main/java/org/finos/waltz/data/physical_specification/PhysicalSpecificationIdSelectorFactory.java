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

package org.finos.waltz.data.physical_specification;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.LOGICAL_FLOW;
import static org.finos.waltz.schema.Tables.TAG_USAGE;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;
import static org.finos.waltz.schema.tables.PhysicalSpecDefn.PHYSICAL_SPEC_DEFN;
import static org.finos.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.common.Checks.checkNotNull;


public class PhysicalSpecificationIdSelectorFactory implements IdSelectorFactory {

    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case ACTOR:
            case APPLICATION:
            case END_USER_APPLICATION:
                return mkForSpecificNode(options);
            case PHYSICAL_FLOW:
                return mkForPhysicalFlow(options);
            case LOGICAL_DATA_ELEMENT:
                return mkForLogicalElement(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case PHYSICAL_SPECIFICATION:
                return mkForSpecification(options);
            case SERVER:
                return mkForServer(options);
            case TAG:
                return mkForTagBasedOnPhysicalFlowTags(options);
            case ALL:
            case APP_GROUP:
            case DATA_TYPE:
            case FLOW_DIAGRAM:
            case ORG_UNIT:
            case MEASURABLE:
            case PERSON:
            case PROCESS_DIAGRAM:
                return mkViaPhysicalFlowSelector(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: " + options);
        }
    }


    private Select<Record1<Long>> mkForSpecificNode(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);

        Condition lifecycleCondition = options.entityLifecycleStatuses().contains(EntityLifecycleStatus.REMOVED)
                ? DSL.trueCondition()
                : PHYSICAL_FLOW.IS_REMOVED.isFalse()
                .and(LOGICAL_FLOW.IS_REMOVED.isFalse()
                        .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse()));

        Condition isSourceOrTarget = (LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(options.entityReference().id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(options.entityReference().kind().name())))
                .or((LOGICAL_FLOW.TARGET_ENTITY_ID.eq(options.entityReference().id())
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(options.entityReference().kind().name()))));

        SelectConditionStep<Record1<Long>> specsUsedByApp = DSL
                .select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(LOGICAL_FLOW).on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where(isSourceOrTarget)
                .and(lifecycleCondition);

        SelectConditionStep<Record1<Long>> specsOwnedByApp = DSL
                .select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(options.entityReference().id())
                        .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(options.entityReference().kind().name())));

        return specsOwnedByApp.union(specsUsedByApp);
    }


    private Select<Record1<Long>> mkForTagBasedOnPhysicalFlowTags(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        long tagId = options.entityReference().id();

        return DSL
                .select(PHYSICAL_SPECIFICATION.ID)
                .from(TAG_USAGE)
                .join(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(TAG_USAGE.ENTITY_ID)
                        .and(TAG_USAGE.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .join(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .where(TAG_USAGE.TAG_ID.eq(tagId))
                .and(getLifecycleCondition(options));
    }


    private Select<Record1<Long>> mkForServer(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);

        long serverId = options.entityReference().id();

        return DSL
                .select(PHYSICAL_FLOW.SPECIFICATION_ID)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(PHYSICAL_FLOW_PARTICIPANT)
                .on(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID.eq(PHYSICAL_FLOW.ID))
                .where(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND.eq(EntityKind.SERVER.name()))
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID.eq(serverId))
                .and(getLifecycleCondition(options));

    }


    private Select<Record1<Long>> mkForLogicalElement(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        long logicalElementId = options.entityReference().id();
        return DSL.select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_SPEC_DEFN).on(PHYSICAL_SPEC_DEFN.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .join(PHYSICAL_SPEC_DEFN_FIELD).on(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(PHYSICAL_SPEC_DEFN.ID))
                .where(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID.eq(logicalElementId));
    }


    private Select<Record1<Long>> mkForSpecification(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkViaPhysicalFlowSelector(IdSelectionOptions options) {
        Select<Record1<Long>> flowSelector = physicalFlowIdSelectorFactory.apply(options);
        Condition condition =
                PHYSICAL_FLOW.ID.in(flowSelector)
                .and(getLifecycleCondition(options));
        return selectViaPhysicalFlowJoin(condition);
    }


    private Select<Record1<Long>> mkForPhysicalFlow(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        long physicalFlowId = options.entityReference().id();
        Condition matchOnPhysFlowId = PHYSICAL_FLOW.ID.eq(physicalFlowId);
        return selectViaPhysicalFlowJoin(matchOnPhysFlowId);

    }


    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        long logicalFlowId = options.entityReference().id();
        Condition matchOnLogicalFlowId =
                PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId)
                .and(getLifecycleCondition(options));
        return selectViaPhysicalFlowJoin(matchOnLogicalFlowId);
    }


    private Select<Record1<Long>> selectViaPhysicalFlowJoin(Condition condition) {
        return DSL
                .select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .where(condition);
    }


    private Condition getLifecycleCondition(IdSelectionOptions options) {
        return options.entityLifecycleStatuses().contains(EntityLifecycleStatus.REMOVED)
                ? DSL.trueCondition()
                : PHYSICAL_FLOW.IS_REMOVED.isFalse()
                .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse());
    }
}
