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

package org.finos.waltz.data.enum_value;


import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.EnumValueRecord;
import org.finos.waltz.model.EnumValue;
import org.finos.waltz.model.ImmutableEnumValue;
import org.finos.waltz.model.enum_value.EnumValueKind;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.schema.tables.EnumValue.ENUM_VALUE;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class EnumValueDao {


    private static final RecordMapper<? super Record, EnumValue> TO_DOMAIN_MAPPER = record -> {
        EnumValueRecord r = record.into(ENUM_VALUE);
        return ImmutableEnumValue
                .builder()
                .type(r.getType())
                .key(r.getKey())
                .name(r.getDisplayName())
                .description(r.getDescription())
                .icon(r.getIconName())
                .iconColor(r.getIconColor())
                .position(r.getPosition())
                .build();
    };


    public static Condition mkExistsCondition(EnumValueKind kind, String key) {
        return DSL.exists(
                DSL.select(ENUM_VALUE.fields())
                        .from(Tables.ENUM_VALUE)
                        .where(Tables.ENUM_VALUE.TYPE.eq(kind.dbValue()))
                        .and(Tables.ENUM_VALUE.KEY.eq(key)));
    }


    private final DSLContext dsl;


    @Autowired
    public EnumValueDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EnumValue> findAll() {
        return dsl
                .select(ENUM_VALUE.fields())
                .from(ENUM_VALUE)
                .fetch()
                .map(TO_DOMAIN_MAPPER);
    }

}
