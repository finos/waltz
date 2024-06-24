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

package org.finos.waltz.data.person;


import org.finos.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import static org.finos.waltz.model.EntityKind.PERSON;

public class PersonIdSelectorFactory extends AbstractIdSelectorFactory {


    public PersonIdSelectorFactory() {
        super(PERSON);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            default:
                throw new UnsupportedOperationException("Cannot create person selector from kind: " + options.entityReference().kind());
        }
    }
}
