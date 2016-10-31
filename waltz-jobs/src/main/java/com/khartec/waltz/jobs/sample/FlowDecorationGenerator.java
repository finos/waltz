package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.DataFlowDecoratorRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.Tables.*;

/**
 * Created by dwatkins on 29/09/2016.
 */
public class FlowDecorationGenerator {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> flowIds = dsl
                .select(DATA_FLOW.ID)
                .from(DATA_FLOW)
                .fetch(DATA_FLOW.ID);

        List<Long> typeIds = dsl
                .select(DATA_TYPE.ID)
                .from(DATA_TYPE)
                .fetch(DATA_TYPE.ID);

        List<DataFlowDecoratorRecord> records = map(
                flowIds,
                id -> {
                    DataFlowDecoratorRecord record = dsl.newRecord(DATA_FLOW_DECORATOR);
                    record.setDataFlowId(id);
                    record.setDecoratorEntityId(randomPick(typeIds));
                    record.setDecoratorEntityKind(EntityKind.DATA_TYPE.name());
                    record.setProvenance("sample");
                    return record;
                });


        dsl.deleteFrom(DATA_FLOW_DECORATOR)
                .where(DATA_FLOW_DECORATOR.PROVENANCE.eq("sample"))
                .execute();
        System.out.println("--- saving: "+records.size());
        dsl.batchStore(records).execute();
        System.out.println("--- done");

    }
}
