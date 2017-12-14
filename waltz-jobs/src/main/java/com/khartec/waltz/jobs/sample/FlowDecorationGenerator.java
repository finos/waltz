/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.Tables.*;


public class FlowDecorationGenerator {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

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
                    record.setProvenance("sample");
                    return record;
                });


        dsl.deleteFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.PROVENANCE.eq("sample"))
                .execute();
        System.out.println("--- saving: "+records.size());
        dsl.batchStore(records).execute();
        System.out.println("--- done");

    }
}
