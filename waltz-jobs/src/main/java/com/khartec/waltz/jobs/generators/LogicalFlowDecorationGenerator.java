/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
