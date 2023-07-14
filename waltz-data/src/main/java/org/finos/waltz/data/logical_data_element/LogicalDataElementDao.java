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

package org.finos.waltz.data.logical_data_element;

import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.FieldDataType;
import org.finos.waltz.model.logical_data_element.ImmutableLogicalDataElement;
import org.finos.waltz.model.logical_data_element.LogicalDataElement;
import org.finos.waltz.schema.tables.records.LogicalDataElementRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.LOGICAL_DATA_ELEMENT;


@Repository
public class LogicalDataElementDao {

    public static final RecordMapper<Record, LogicalDataElement> TO_DOMAIN_MAPPER = r -> {
        LogicalDataElementRecord record = r.into(LOGICAL_DATA_ELEMENT);
        return ImmutableLogicalDataElement.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .name(record.getName())
                .description(record.getDescription())
                .type(FieldDataType.valueOf(record.getType()))
                .parentDataTypeId(record.getParentDataTypeId())
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getEntityLifecycleStatus()))
                .provenance(record.getProvenance())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public LogicalDataElementDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public LogicalDataElement getById(long id) {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public LogicalDataElement getByExternalId(String externalId) {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LogicalDataElement> findAll() {
        return dsl.select()
                .from(LOGICAL_DATA_ELEMENT)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LogicalDataElement> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(LOGICAL_DATA_ELEMENT.fields())
                .from(LOGICAL_DATA_ELEMENT)
                .where(LOGICAL_DATA_ELEMENT.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
