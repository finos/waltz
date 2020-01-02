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

package com.khartec.waltz.data.attribute_change;


import com.khartec.waltz.model.attribute_change.AttributeChange;
import com.khartec.waltz.model.attribute_change.ImmutableAttributeChange;
import com.khartec.waltz.schema.tables.records.AttributeChangeRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.AttributeChange.ATTRIBUTE_CHANGE;


@Repository
public class AttributeChangeDao {

    private final DSLContext dsl;


    public static final RecordMapper<Record, AttributeChange> TO_DOMAIN_MAPPER = r -> {
        AttributeChangeRecord record = r.into(AttributeChangeRecord.class);

        return ImmutableAttributeChange.builder()
                .id(record.getId())
                .changeUnitId(record.getChangeUnitId())
                .type(record.getType())
                .newValue(record.getNewValue())
                .oldValue(record.getOldValue())
                .name(record.getName())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    public static final Function<AttributeChange, AttributeChangeRecord> TO_RECORD_MAPPER = attributeChange -> {
        AttributeChangeRecord record = new AttributeChangeRecord();
        attributeChange.id().ifPresent(record::setId);
        record.setChangeUnitId(attributeChange.changeUnitId());
        record.setType(attributeChange.type());
        record.setNewValue(attributeChange.newValue());
        record.setOldValue(attributeChange.oldValue());
        record.setName(attributeChange.name());
        record.setLastUpdatedAt(Timestamp.valueOf(attributeChange.lastUpdatedAt()));
        record.setLastUpdatedBy(attributeChange.lastUpdatedBy());
        record.setProvenance(attributeChange.provenance());

        return record;
    };


    @Autowired
    public AttributeChangeDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public AttributeChange getById(long id) {
        return dsl
                .selectFrom(ATTRIBUTE_CHANGE)
                .where(ATTRIBUTE_CHANGE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<AttributeChange> findByChangeUnitId(long id) {
        return dsl
                .selectFrom(ATTRIBUTE_CHANGE)
                .where(ATTRIBUTE_CHANGE.CHANGE_UNIT_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
