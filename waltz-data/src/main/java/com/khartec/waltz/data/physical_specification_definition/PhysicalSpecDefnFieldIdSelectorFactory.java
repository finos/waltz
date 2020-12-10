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

package com.khartec.waltz.data.physical_specification_definition;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.PhysicalSpecDefnField.PHYSICAL_SPEC_DEFN_FIELD;


public class PhysicalSpecDefnFieldIdSelectorFactory implements IdSelectorFactory {

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
        ensureScopeIsExact(options);
        long logicalElementId = options.entityReference().id();
        return DSL.select(PHYSICAL_SPEC_DEFN_FIELD.ID)
                .from(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.LOGICAL_DATA_ELEMENT_ID.eq(logicalElementId));
    }


    private Select<Record1<Long>> mkForPhysicalSpecDefn(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long specDefnId = options.entityReference().id();
        return DSL.select(PHYSICAL_SPEC_DEFN_FIELD.ID)
                .from(PHYSICAL_SPEC_DEFN_FIELD)
                .where(PHYSICAL_SPEC_DEFN_FIELD.SPEC_DEFN_ID.eq(specDefnId));
    }

}
