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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.common.SetUtilities.uniqBy;
import static com.khartec.waltz.schema.Tables.DATA_TYPE;
import static com.khartec.waltz.schema.Tables.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;

/**
 * Created by dwatkins on 29/09/2016.
 */
public class LogicalFlowDecorationGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> flowIds = dsl
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .fetch(LOGICAL_FLOW.ID);

        List<Long> typeIds = dsl
                .select(DATA_TYPE.ID)
                .from(DATA_TYPE)
                .fetch(DATA_TYPE.ID);

        List<LogicalFlowDecoratorRecord> records = map(
                flowIds,
                id -> {
                    LogicalFlowDecoratorRecord record = dsl.newRecord(LOGICAL_FLOW_DECORATOR);
                    record.setLogicalFlowId(id);
                    record.setDecoratorEntityId(randomPick(typeIds));
                    record.setDecoratorEntityKind(EntityKind.DATA_TYPE.name());
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setLastUpdatedBy("admin");
                    record.setLastUpdatedAt(Timestamp.from(Instant.now()));
                    return record;
                });

        Set<LogicalFlowDecoratorRecord> deduped = uniqBy(records, r ->
                "lfd:"
                        + r.getLogicalFlowId() + "_"
                        + r.getDecoratorEntityId() + "_"
                        + r.getDecoratorEntityKind());

        log("--- saving: " + deduped.size());
        dsl.batchStore(records)
                .execute();
        log("--- done");

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }
}
