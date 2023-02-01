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

package org.finos.waltz.data;

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.Tables;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Service which takes an entity kind and returns aliases
 */
@Repository
public class EntityAliasPopulator {

    private final DSLContext dsl;

    @Autowired
    public EntityAliasPopulator(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Map<String, Long> fetchEntityIdLookupMap(EntityKind entityKind, Set<String> identifiers) {
        checkNotNull(entityKind, "entityKind cannot be null");

        switch (entityKind) {
            case APPLICATION:
                return fetchApplicationAliasToIdMap(identifiers);
            case PERSON:
                return FunctionUtilities.time("fetch alias map people", () -> fetchPersonAliasToIdMap(identifiers));
            default:
                throw new IllegalArgumentException(format("Cannot find lookup map for id for entity kind: %s", entityKind));
        }
    }

    private Map<String, Long> fetchApplicationAliasToIdMap(Set<String> aliases) {
        return dsl
                .select(Tables.APPLICATION.ASSET_CODE, Tables.APPLICATION.ID)
                .from(Tables.APPLICATION)
                .where(Tables.APPLICATION.ASSET_CODE.in(aliases))
                .fetchSet(r -> tuple(r.get(Tables.APPLICATION.ASSET_CODE), r.get(Tables.APPLICATION.ID)))
                .stream()
                .collect(toMap(t -> t.v1, t -> t.v2, (v1, v2) -> v1));
    }


    private Map<String, Long> fetchPersonAliasToIdMap(Set<String> aliases) {

        SelectConditionStep<Record2<String, Long>> emails = dsl
                .select(Tables.PERSON.EMAIL, Tables.PERSON.ID)
                .from(Tables.PERSON)
                .where(Tables.PERSON.IS_REMOVED.isFalse()
                        .and(Tables.PERSON.EMAIL.in(aliases)));

        SelectConditionStep<Record2<String, Long>> name = dsl
                .select(Tables.PERSON.DISPLAY_NAME, Tables.PERSON.ID)
                .from(Tables.PERSON)
                .where(Tables.PERSON.IS_REMOVED.isFalse()
                        .and(Tables.PERSON.DISPLAY_NAME.in(aliases)));

        SelectConditionStep<Record2<String, Long>> empId = dsl
                .select(Tables.PERSON.EMPLOYEE_ID, Tables.PERSON.ID)
                .from(Tables.PERSON)
                .where(Tables.PERSON.IS_REMOVED.isFalse()
                        .and(Tables.PERSON.EMPLOYEE_ID.in(aliases)));

        SelectOrderByStep<Record2<String, Long>> qry = emails.union(name.union(empId));

        return qry
                .fetchSet(r -> tuple(r.get(0, String.class), r.get(Tables.PERSON.ID)))
                .stream()
                .collect(toMap(t -> t.v1, t -> t.v2, (v1, v2) -> v1));
    }

}
