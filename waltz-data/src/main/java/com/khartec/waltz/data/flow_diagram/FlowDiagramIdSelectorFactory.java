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

package com.khartec.waltz.data.flow_diagram;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


public class FlowDiagramIdSelectorFactory implements IdSelectorFactory {

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case ACTOR:
            case APPLICATION:
            case LOGICAL_DATA_FLOW:
            case PHYSICAL_FLOW:
            case MEASURABLE:
                return mkForDirectEntity(options);
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical flow selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForDirectEntity(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        EntityReference ref = options.entityReference();
        return DSL
                .select(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(ref.id()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(ref.kind().name()));
    }


    private Select<Record1<Long>> mkForPhysicalSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long specificationId = options.entityReference().id();
        return DSL
                .select(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .innerJoin(PHYSICAL_FLOW)
                .on(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(PHYSICAL_FLOW.ID)
                        .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specificationId));
    }

}
