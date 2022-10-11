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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        Field<String> alias = DSL.field("alias", String.class);

        SelectJoinStep<Record1<String>> aliasesTable = DSL
                .select(alias)
                .from(DSL.values(mkRowsFromIdentifiers(identifiers)).as("aliases", "alias"));

        switch (entityKind) {
            case APPLICATION:
                return fetchApplicationAliasToIdMap(alias, aliasesTable);
            case PERSON:
                return fetchPersonAliasToIdMap(alias, aliasesTable);
            default:
                throw new IllegalArgumentException(format("Cannot find lookup map for id for entity kind: %s", entityKind));
        }
    }

    private Map<String, Long> fetchApplicationAliasToIdMap(Field<String> alias, SelectJoinStep<Record1<String>> aliasesTable) {
        return dsl
                .select(aliasesTable.field(alias), Tables.APPLICATION.ID)
                .from(Tables.APPLICATION)
                .innerJoin(aliasesTable).on(aliasesTable.field(alias).eq(Tables.APPLICATION.ASSET_CODE))
                .fetchSet(r -> tuple(r.get(aliasesTable.field(alias)), r.get(Tables.APPLICATION.ID)))
                .stream()
                .collect(toMap(t -> t.v1, t -> t.v2, (v1, v2) -> v1));
    }


    private Map<String, Long> fetchPersonAliasToIdMap(Field<String> alias, SelectJoinStep<Record1<String>> aliasesTable) {

        Condition joinCond = aliasesTable.field(alias).equalIgnoreCase(Tables.PERSON.EMPLOYEE_ID)
                .or(aliasesTable.field(alias).equalIgnoreCase(Tables.PERSON.EMAIL)
                        .or(aliasesTable.field(alias).equalIgnoreCase(Tables.PERSON.DISPLAY_NAME)));

        return dsl
                .select(aliasesTable.field(alias), Tables.PERSON.ID)
                .from(Tables.PERSON)
                .innerJoin(aliasesTable).on(joinCond)
                .where(Tables.PERSON.IS_REMOVED.isFalse())
                .fetchSet(r -> tuple(r.get(aliasesTable.field(alias)), r.get(Tables.PERSON.ID)))
                .stream()
                .collect(toMap(t -> t.v1, t -> t.v2, (v1, v2) -> v1));
    }

    private Row1<String>[] mkRowsFromIdentifiers(Set<String> identifiers) {
        return identifiers
                .stream()
                .map(d -> DSL.row(d))
                .collect(Collectors.toSet())
                .toArray(new Row1[0]);
    }

}
