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

package org.finos.waltz.jobs.tools.resolvers;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.PERSON;

public class PersonNameToEmpIdResolver implements Resolver<String> {

    private final Map<String, String> personToEmpId;


    public PersonNameToEmpIdResolver(DSLContext dsl) {
        checkNotNull(dsl, "DSL cannot be null");
        personToEmpId = loadPersonToEmpIdMap(dsl);
    }


    @Override
    public Optional<String> resolve(String name) {
        return Optional.ofNullable(personToEmpId.get(normalize(name)));
    }


    private Map<String, String> loadPersonToEmpIdMap(DSLContext dsl) {
        Field<String> normalizedName = DSL.trim(DSL.lower(PERSON.DISPLAY_NAME));
        return dsl
                .select(normalizedName, PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.IS_REMOVED.isFalse())
                .fetchMap(normalizedName, PERSON.EMPLOYEE_ID);
    }


}
