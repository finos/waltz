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

package org.finos.waltz.data.change_set;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.change_unit.ChangeUnitIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.CHANGE_SET;
import static org.finos.waltz.schema.tables.ChangeUnit.CHANGE_UNIT;
import static org.finos.waltz.common.Checks.checkNotNull;

public class ChangeSetIdSelectorFactory implements IdSelectorFactory {

    private final ChangeUnitIdSelectorFactory changeUnitIdSelectorFactory = new ChangeUnitIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        switch(options.entityReference().kind()) {
            case CHANGE_INITIATIVE:
                // all physical flows where the app is a source or target
                return mkForChangeInitiative(options);
            case ALL:
            case APPLICATION:
            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
                return mkByChangeUnitSelector(options);
            default:
                throw new UnsupportedOperationException("Cannot create Change Unit selector from options: " + options);
        }
    }


    private Select<Record1<Long>> mkByChangeUnitSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> cuSelector = changeUnitIdSelectorFactory.apply(options);
        return DSL.selectDistinct(CHANGE_UNIT.CHANGE_SET_ID)
                .from(CHANGE_UNIT)
                .where(CHANGE_UNIT.ID.in(cuSelector));
    }


    private SelectConditionStep<Record1<Long>> mkForChangeInitiative(IdSelectionOptions options) {
        return DSL.selectDistinct(CHANGE_SET.ID)
                .from(CHANGE_SET)
                .where(CHANGE_SET.PARENT_ENTITY_ID.in(options.entityReference().id()))
                .and(CHANGE_SET.PARENT_ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()));
    }
}
