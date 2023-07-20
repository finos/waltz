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

package org.finos.waltz.data.change_unit;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.tables.ChangeUnit.CHANGE_UNIT;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.common.Checks.checkNotNull;

public class ChangeUnitIdSelectorFactory implements IdSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();



    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case ACTOR:
            case APPLICATION:
                // all physical flows where the app is a source or target
                return mkForFlowEndpoint(options);
            case ALL:
            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case ORG_UNIT:
                return mkByAppSelector(options);
            case PERSON:
                return mkByAppSelector(options);
            case PHYSICAL_FLOW:
                return mkForDirectEntity(options);
            case CHANGE_SET:
                return mkForChangeSet(options);
            default:
                throw new UnsupportedOperationException("Cannot create Change Unit selector from options: " + options);
        }
    }

    private Select<Record1<Long>> mkForChangeSet(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL
                .select(CHANGE_UNIT.ID)
                .from(CHANGE_UNIT)
                .where(CHANGE_UNIT.CHANGE_SET_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkByAppSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(options);

        Condition sourceRef = LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appSelector));

        Condition targetRef = LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.in(appSelector));

        return DSL
                .select(CHANGE_UNIT.ID)
                .from(CHANGE_UNIT)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_FLOW.ID.eq(CHANGE_UNIT.SUBJECT_ENTITY_ID)
                        .and(CHANGE_UNIT.SUBJECT_ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(sourceRef.or(targetRef));
    }


    private Select<Record1<Long>> mkForDirectEntity(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        EntityReference ref = options.entityReference();
        return DSL
                .select(CHANGE_UNIT.ID)
                .from(CHANGE_UNIT)
                .where(CHANGE_UNIT.SUBJECT_ENTITY_ID.eq(ref.id()))
                .and(CHANGE_UNIT.SUBJECT_ENTITY_KIND.eq(ref.kind().name()));
    }


    private Select<Record1<Long>> mkForFlowEndpoint(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        EntityReference ref = options.entityReference();

        Condition sourceRef = LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id()));

        Condition targetRef = LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name())
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id()));

        return DSL
                .select(CHANGE_UNIT.ID)
                .from(CHANGE_UNIT)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_FLOW.ID.eq(CHANGE_UNIT.SUBJECT_ENTITY_ID)
                        .and(CHANGE_UNIT.SUBJECT_ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(sourceRef.or(targetRef));
    }

}
