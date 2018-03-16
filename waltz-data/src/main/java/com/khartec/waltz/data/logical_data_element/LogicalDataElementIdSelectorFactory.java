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

package com.khartec.waltz.data.logical_data_element;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.LogicalDataElement.LOGICAL_DATA_ELEMENT;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefn.PHYSICAL_SPEC_DEFN;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;


@Service
public class LogicalDataElementIdSelectorFactory implements IdSelectorFactory {

    @Autowired
    public LogicalDataElementIdSelectorFactory() {
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case PHYSICAL_SPECIFICATION:
                return mkForSpecification(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForSpecification(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given a spec ref");

        return DSL
                .select(LOGICAL_DATA_ELEMENT.ID)
                .from(LOGICAL_DATA_ELEMENT)
                .innerJoin(PHYSICAL_SPEC_DEFN_FIELD).on(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID.eq(LOGICAL_DATA_ELEMENT.ID))
                .innerJoin(PHYSICAL_SPEC_DEFN).on(PHYSICAL_SPEC_DEFN.ID.eq(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID))
                .where(PHYSICAL_SPEC_DEFN.SPECIFICATION_ID.eq(options.entityReference().id()));
    }

}
