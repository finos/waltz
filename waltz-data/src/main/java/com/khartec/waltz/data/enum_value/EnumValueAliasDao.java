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

package com.khartec.waltz.data.enum_value;


import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.model.enum_value.EnumValueKind;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.Optional.ofNullable;

@Repository
public class EnumValueAliasDao {

    private final DSLContext dsl;


    @Autowired
    public EnumValueAliasDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Aliases<String> mkAliases(EnumValueKind kind) {
        Aliases<String> aliases = new Aliases<>();

        // join on ENUM_VALUE in case there are no aliases
        dsl.select(ENUM_VALUE.KEY, DSL.coalesce(ENUM_VALUE_ALIAS.ALIAS, ENUM_VALUE.KEY))
                .from(ENUM_VALUE)
                .leftOuterJoin(ENUM_VALUE_ALIAS)
                .on(ENUM_VALUE.KEY.eq(ENUM_VALUE_ALIAS.ENUM_KEY))
                .where(ENUM_VALUE.TYPE.eq(kind.dbValue()))
                .fetch()
                .forEach(r -> aliases.register(
                        r.value1(),
                        r.value2()));

        return aliases;

    }

}
