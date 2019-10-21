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

package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefn.PHYSICAL_SPEC_DEFN;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


public class PhysicalSpecificationIdSelectorFactory implements IdSelectorFactory {

    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case PHYSICAL_FLOW:
                return mkForPhysicalFlow(options);
            case LOGICAL_DATA_ELEMENT:
                return mkForLogicalElement(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case PHYSICAL_SPECIFICATION:
                return mkForSpecification(options);
            case SERVER:
                return mkForServer(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForServer(IdSelectionOptions options) {
        ensureScopeIsExact(options);

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
        ensureScopeIsExact(options);
        long logicalElementId = options.entityReference().id();
        return DSL.select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .join(PHYSICAL_SPEC_DEFN).on(PHYSICAL_SPEC_DEFN.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .join(PHYSICAL_SPEC_DEFN_FIELD).on(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(PHYSICAL_SPEC_DEFN.ID))
                .where(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID.eq(logicalElementId));
    }


    private Select<Record1<Long>> mkForSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        Select<Record1<Long>> flowSelector = physicalFlowIdSelectorFactory.apply(options);
        Condition condition =
                PHYSICAL_FLOW.ID.in(flowSelector)
                .and(getLifecycleCondition(options));
        return selectViaPhysicalFlowJoin(condition);
    }


    private Select<Record1<Long>> mkForPhysicalFlow(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long physicalFlowId = options.entityReference().id();
        Condition matchOnPhysFlowId = PHYSICAL_FLOW.ID.eq(physicalFlowId);
        return selectViaPhysicalFlowJoin(matchOnPhysFlowId);

    }


    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        ensureScopeIsExact(options);
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
