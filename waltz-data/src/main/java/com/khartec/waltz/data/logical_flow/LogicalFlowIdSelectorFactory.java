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

package com.khartec.waltz.data.logical_flow;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


@Service
public class LogicalFlowIdSelectorFactory implements IdSelectorFactory {


    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;

    private final static Application CONSUMER_APP = APPLICATION.as("consumer");
    private final static Application SUPPLIER_APP = APPLICATION.as("supplier");


    @Autowired
    public LogicalFlowIdSelectorFactory(ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                        DataTypeIdSelectorFactory dataTypeIdSelectorFactory) {
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");

        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
    }


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
                ApplicationIdSelectionOptions appOptions = ApplicationIdSelectionOptions.mkOpts(options);
                return wrapAppIdSelector(appOptions);
            case DATA_TYPE:
                return mkForDataType(ApplicationIdSelectionOptions.mkOpts(options));
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case PHYSICAL_SPECIFICATION:
                return mkForPhysicalSpecification(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: " + options);
        }
    }



    private Select<Record1<Long>> mkForPhysicalSpecification(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(options.entityReference().id()))
                .and(NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(LOGICAL_FLOW.ID))
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.LOGICAL_DATA_FLOW.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(options.entityReference().id()))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.in(map(options.entityLifecycleStatuses(), e -> e.name())));
    }


    private Select<Record1<Long>> wrapAppIdSelector(ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);

        Condition sourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition targetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(sourceCondition.or(targetCondition))
                .and(NOT_REMOVED);
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
                .and(NOT_REMOVED);
    }


    private Select<Record1<Long>> mkForDataType(ApplicationIdSelectionOptions options) {
        Select<Record1<Long>> dataTypeSelector = dataTypeIdSelectorFactory.apply(options);

        Condition supplierNotRemoved =  SUPPLIER_APP.IS_REMOVED.isFalse();
        Condition consumerNotRemoved =  CONSUMER_APP.IS_REMOVED.isFalse();

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
                .and(SUPPLIER_APP.KIND.in(options.applicationKinds()).or(CONSUMER_APP.KIND.in(options.applicationKinds())))
                .and(NOT_REMOVED)
                .and(supplierNotRemoved)
                .and(consumerNotRemoved);
    }

}
