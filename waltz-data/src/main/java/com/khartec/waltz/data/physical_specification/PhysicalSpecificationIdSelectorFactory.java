/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Service
public class PhysicalSpecificationIdSelectorFactory implements IdSelectorFactory {

    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory;


    @Autowired
    public PhysicalSpecificationIdSelectorFactory(PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory) {
        checkNotNull(physicalFlowIdSelectorFactory, "physicalFlowIdSelectorFactory cannot be null");
        this.physicalFlowIdSelectorFactory = physicalFlowIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case PHYSICAL_FLOW:
                return mkForPhysicalFlow(options);
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case PHYSICAL_SPECIFICATION:
                return mkForSpecification(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForSpecification(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given a spec ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        Select<Record1<Long>> flowSelector = physicalFlowIdSelectorFactory.apply(options);
        Condition condition = PHYSICAL_FLOW.ID.in(flowSelector);
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
        Condition matchOnLogicalFlowId = PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId);
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

}
