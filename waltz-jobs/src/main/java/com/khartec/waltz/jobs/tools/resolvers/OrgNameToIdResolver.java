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

package com.khartec.waltz.jobs.tools.resolvers;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;

public class OrgNameToIdResolver implements Resolver<Long> {

    private final Map<String, Long> orgToIdMap;


    public OrgNameToIdResolver(DSLContext dsl) {
        checkNotNull(dsl, "DSL cannot be null");
        orgToIdMap = loadMap(dsl);
    }


    @Override
    public Optional<Long> resolve(String name) {
        return Optional.ofNullable(orgToIdMap.get(normalize(name)));
    }


    private Map<String, Long> loadMap(DSLContext dsl) {
        Field<String> normalizedName = DSL.trim(DSL.lower(ORGANISATIONAL_UNIT.NAME));
        Map<String, Long> result = new HashMap<>();
        dsl.select(normalizedName, ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch()
                .forEach(r -> result.put(r.get(normalizedName), r.get(ORGANISATIONAL_UNIT.ID)));

        return result;
    }
}

