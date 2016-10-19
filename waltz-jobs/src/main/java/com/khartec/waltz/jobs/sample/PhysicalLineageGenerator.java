package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.schema.tables.records.PhysicalFlowLineageRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;

public class PhysicalLineageGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<PhysicalFlow> physicalFlows = dsl.select(PHYSICAL_FLOW.fields())
                .select(APPLICATION.NAME)
                .from(PHYSICAL_FLOW)
                .join(APPLICATION).on(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(APPLICATION.ID))
                .fetch(PhysicalFlowDao.TO_DOMAIN_MAPPER);


        List<PhysicalFlowLineageRecord> records = physicalFlows.stream()
                .flatMap(f -> {
                    return IntStream.range(0, rnd.nextInt(5))
                            .mapToObj(i -> {
                                PhysicalFlow contributor = ListUtilities.randomPick(physicalFlows);

                                PhysicalFlowLineageRecord record = new PhysicalFlowLineageRecord();
                                record.setContributorFlowId(contributor.id().get());
                                record.setDescribedFlowId(f.id().get());
                                record.setDescription(String.format("%s described by %s", f, contributor));
                                record.setLastUpdatedBy("admin");
                                return record;
                            });
                })
                .filter(r -> !r.getDescribedFlowId().equals(r.getContributorFlowId()))
                .distinct()
                .collect(Collectors.toList());



        System.out.println("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW_LINEAGE)
                .execute();

        System.out.println("---saving record: "+records.size());
        dsl.batchInsert(records).execute();

        System.out.println("---done");


    }
}
