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

package org.finos.waltz.data.physical_specification_definition;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.tables.PhysicalSpecDefn.PHYSICAL_SPEC_DEFN;
import static org.finos.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;
import static org.finos.waltz.common.Checks.checkNotNull;


public class PhysicalSpecDefnIdSelectorFactory implements IdSelectorFactory {

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case LOGICAL_DATA_ELEMENT:
                return mkForLogicalElement(options);
            case PHYSICAL_SPEC_DEFN:
                return mkForPhysicalSpecDefn(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical spec defn field selector from options: "+ options);
        }
    }


    private Select<Record1<Long>> mkForLogicalElement(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        long logicalElementId = options.entityReference().id();
        return DSL
                .select(PHYSICAL_SPEC_DEFN.ID)
                .from(PHYSICAL_SPEC_DEFN)
                .join(PHYSICAL_SPEC_DEFN_FIELD).on(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(PHYSICAL_SPEC_DEFN.ID))
                .where(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID.eq(logicalElementId));
    }


    private Select<Record1<Long>> mkForPhysicalSpecDefn(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL.select(DSL.val(options.entityReference().id()));
    }

}
