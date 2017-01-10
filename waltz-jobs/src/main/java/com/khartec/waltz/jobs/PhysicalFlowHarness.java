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

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlowDeleteCommand;
import com.khartec.waltz.model.physical_flow.PhysicalFlowDeleteCommandResponse;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Function;

import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.select;

public class PhysicalFlowHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        PhysicalFlowService service = ctx.getBean(PhysicalFlowService.class);

        PhysicalFlowDeleteCommandResponse response;
        Select<Record1<Long>> lineageCheckSelect = select(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID)
                .from(PHYSICAL_FLOW_LINEAGE);

        Function<Condition, Select<Record1<Long>>> specIdSelectSupplier = (havingCondition) -> select(PHYSICAL_FLOW.SPECIFICATION_ID)
                .from(PHYSICAL_FLOW)
                .groupBy(PHYSICAL_FLOW.SPECIFICATION_ID)
                .having(havingCondition);


        // invalid flow id
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(-1)
                .build(), "admin");
        System.out.println(response);

        // flow linked to a lineage
        Long lineageFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.in(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().gt(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(lineageFlowId)
                .build(), "admin");
        System.out.println(response);

        // flow id linked to a spec with more than 1 physical flow
        Long multiSpecFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.notIn(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().gt(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(multiSpecFlowId)
                .build(), "admin");
        System.out.println(response);

        // flow id linked to a spec with only 1 physical flow
        Long singleSpecFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.notIn(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().eq(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(singleSpecFlowId)
                .build(), "admin");
        System.out.println(response);
    }

}
