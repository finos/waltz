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

package com.khartec.waltz.data.data_type;


import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

public class DataTypeIdSelectorFactory extends AbstractIdSelectorFactory {


    public DataTypeIdSelectorFactory() {
        super(EntityKind.DATA_TYPE);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        throw new UnsupportedOperationException("Cannot create dataType selector from kind: " +
                options.entityReference().kind());
    }
}
