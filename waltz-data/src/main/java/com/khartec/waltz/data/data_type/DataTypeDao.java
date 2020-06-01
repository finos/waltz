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

import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.datatype.ImmutableDataType;
import com.khartec.waltz.schema.tables.records.DataTypeRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;


@Repository
public class DataTypeDao implements FindEntityReferencesByIdSelector {

    public final static RecordMapper<Record, DataType> TO_DOMAIN = r -> {
        DataTypeRecord record = r.into(DataTypeRecord.class);
        return ImmutableDataType.builder()
                .code(record.getCode())
                .description(mkSafe(record.getDescription()))
                .name(record.getName())
                .id(Optional.ofNullable(record.getId()))
                .parentId(Optional.ofNullable(record.getParentId()))
                .deprecated(record.getDeprecated())
                .concrete(record.getConcrete())
                .unknown(record.getUnknown())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public DataTypeDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<DataType> findAll() {
        return dsl
                .selectFrom(DATA_TYPE)
                .fetch(TO_DOMAIN);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");

        return dsl
                .select(DATA_TYPE.ID, DATA_TYPE.NAME, DSL.val(EntityKind.DATA_TYPE.name()))
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }


    public DataType getByCode(String code) {
        checkNotEmpty(code, "Code cannot be null/empty");
        return dsl
                .selectFrom(DATA_TYPE)
                .where(DATA_TYPE.CODE.eq(code))
                .fetchOne(TO_DOMAIN);
    }


    public DataType getById(long dataTypeId) {
        return dsl
                .selectFrom(DATA_TYPE)
                .where(DATA_TYPE.ID.eq(dataTypeId))
                .fetchOne(TO_DOMAIN);
    }


    public List<DataType> findByIds(Collection<Long> ids) {
        return dsl
                .selectFrom(DATA_TYPE)
                .where(DATA_TYPE.ID.in(ids))
                .fetch(TO_DOMAIN);
    }
}
