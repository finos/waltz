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

package org.finos.waltz.data.data_type;


import org.finos.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;
import static org.finos.waltz.data.SelectorUtilities.ensureScopeIsExact;

public class DataTypeIdSelectorFactory extends AbstractIdSelectorFactory {


    public DataTypeIdSelectorFactory() {
        super(EntityKind.DATA_TYPE);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            default:
                throw new UnsupportedOperationException("Cannot create dataType selector from kind: " +
                        options.entityReference().kind());
        }

    }


    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .selectDistinct(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                .from(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForPhysicalSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .selectDistinct(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID)
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(options.entityReference().id()));
    }
}
