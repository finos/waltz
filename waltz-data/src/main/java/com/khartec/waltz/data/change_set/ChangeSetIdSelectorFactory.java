/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.data.change_set;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.change_unit.ChangeUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.CHANGE_SET;
import static com.khartec.waltz.schema.tables.ChangeUnit.CHANGE_UNIT;

public class ChangeSetIdSelectorFactory implements IdSelectorFactory {

    private final ChangeUnitIdSelectorFactory changeUnitIdSelectorFactory = new ChangeUnitIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        switch(options.entityReference().kind()) {
            case CHANGE_INITIATIVE:
                // all physical flows where the app is a source or target
                return mkForChangeInitiative(options);
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
