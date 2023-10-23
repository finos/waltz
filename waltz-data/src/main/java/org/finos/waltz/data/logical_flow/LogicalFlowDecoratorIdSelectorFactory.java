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

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;


public class LogicalFlowDecoratorIdSelectorFactory implements IdSelectorFactory {

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch (options.entityReference().kind()) {
            case LOGICAL_DATA_FLOW:
                return mkForLogicalFlow(options);
            default:
                throw new UnsupportedOperationException("Cannot create logical flow decorator selector from options: " + options);
        }
    }

    private Select<Record1<Long>> mkForLogicalFlow(IdSelectionOptions options) {
        return DSL.select(LOGICAL_FLOW_DECORATOR.ID)
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(options.entityReference().id()))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses()));
    }


}
