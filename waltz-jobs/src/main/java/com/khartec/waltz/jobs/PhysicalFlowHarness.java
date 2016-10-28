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
                .build());
        System.out.println(response);

        // flow linked to a lineage
        Long lineageFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.in(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().gt(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(lineageFlowId)
                .build());
        System.out.println(response);

        // flow id linked to a spec with more than 1 physical flow
        Long multiSpecFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.notIn(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().gt(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(multiSpecFlowId)
                .build());
        System.out.println(response);

        // flow id linked to a spec with only 1 physical flow
        Long singleSpecFlowId = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.notIn(lineageCheckSelect))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.in(specIdSelectSupplier.apply(count().eq(1))))
                .fetchAny(PHYSICAL_FLOW.ID);
        response = service.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                .flowId(singleSpecFlowId)
                .build());
        System.out.println(response);
    }

}
