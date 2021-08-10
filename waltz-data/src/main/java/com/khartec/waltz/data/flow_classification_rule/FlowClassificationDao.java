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

package com.khartec.waltz.data.flow_classification_rule;

import com.khartec.waltz.model.flow_classification.FlowClassification;
import com.khartec.waltz.model.flow_classification.ImmutableFlowClassification;
import com.khartec.waltz.schema.tables.records.FlowClassificationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.FLOW_CLASSIFICATION;


@Repository
public class FlowClassificationDao {

    private final DSLContext dsl;

    private static final RecordMapper<Record, FlowClassification> TO_DOMAIN_MAPPER = r -> {
        FlowClassificationRecord record = r.into(FlowClassificationRecord.class);
        return ImmutableFlowClassification.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .code(record.getCode())
                .color(record.getColor())
                .position(record.getPosition())
                .isCustom(record.getIsCustom())
                .build();
    };


    @Autowired
    public FlowClassificationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public Set<FlowClassification> findAll() {
        return dsl
                .selectFrom(FLOW_CLASSIFICATION)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public FlowClassification getById(long id) {
        return dsl
                .selectFrom(FLOW_CLASSIFICATION)
                .where(FLOW_CLASSIFICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public int remove(long id) {
        return dsl
                .delete(FLOW_CLASSIFICATION)
                .where(FLOW_CLASSIFICATION.ID.eq(id))
                .execute();
    }

}
