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

package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;


public class PhysicalFlowIdSelectorFactory implements IdSelectorFactory {

    public static final Condition PHYSICAL_FLOW_NOT_REMOVED = PHYSICAL_FLOW.IS_REMOVED.isFalse()
            .and(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case SERVER:
                return mkForServer(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical flow selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForServer(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long serverId = options.entityReference().id();

        Condition lifecycleCondition = options.entityLifecycleStatuses().contains(EntityLifecycleStatus.REMOVED)
            ? DSL.trueCondition()
            : PHYSICAL_FLOW.IS_REMOVED.isFalse();

        return DSL
                .select(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID)
                .from(PHYSICAL_FLOW_PARTICIPANT)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.ID.eq(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID))
                .where(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND.eq(EntityKind.SERVER.name()))
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID.eq(serverId))
                .and(lifecycleCondition);
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long diagramId = options.entityReference().id();
        return DSL
                .select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(PHYSICAL_FLOW.ID.eq(FLOW_DIAGRAM_ENTITY.ENTITY_ID))
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name()))
                .and(PHYSICAL_FLOW_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long logicalFlowId = options.entityReference().id();
        return DSL
                .select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId))
                .and(PHYSICAL_FLOW_NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForPhysicalSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long specificationId = options.entityReference().id();
        return DSL
                .select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specificationId));
    }

}
