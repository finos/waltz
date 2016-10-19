package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.randomPick;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


public class PhysicalFlowGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<PhysicalSpecification> specifications = dsl.select(PHYSICAL_SPECIFICATION.fields())
                .from(PHYSICAL_SPECIFICATION)
                .fetch(PhysicalSpecificationDao.TO_DOMAIN_MAPPER);

        List<Tuple3<Long, Long, Long>> allLogicalFLows = dsl.select(DATA_FLOW.ID, DATA_FLOW.SOURCE_ENTITY_ID, DATA_FLOW.TARGET_ENTITY_ID)
                .from(DATA_FLOW)
                .fetch(r -> Tuple.tuple(
                        r.getValue(DATA_FLOW.ID),
                        r.getValue(DATA_FLOW.SOURCE_ENTITY_ID),
                        r.getValue(DATA_FLOW.TARGET_ENTITY_ID)));

        Map<Long, Collection<Long>> targetsBySourceApp = groupBy(
                t -> t.v2(),
                t -> t.v3(),
                allLogicalFLows);

        List<PhysicalFlowRecord> records = specifications.stream()
                .map(a -> {
                    Collection<Long> targetIds = targetsBySourceApp.get(a.owningEntity().id());
                    if (isEmpty(targetIds)) return null;
                    return Tuple.tuple(a.id().get(), targetIds);
                })
                .filter(t -> t != null)
                .flatMap(t -> {
                    List<Long> targetIds = new LinkedList(t.v2);
                    return IntStream.range(0, t.v2.size() - 1)
                        .mapToObj(i -> {

                            PhysicalFlowRecord record = dsl.newRecord(PHYSICAL_FLOW);
                            record.setSpecificationId(t.v1);
                            record.setTargetEntityId(targetIds.remove(rnd.nextInt(targetIds.size() - 1)));
                            record.setTargetEntityKind(EntityKind.APPLICATION.name());
                            record.setDescription("Description: " + t.v1 + " - " + t.v2);
                            record.setProvenance("DEMO");
                            record.setBasisOffset(randomPick(newArrayList(0, 0, 0, 0, 1, 1, 2, -1)));
                            record.setTransport(ArrayUtilities.randomPick(TransportKind.values()).name());
                            record.setFrequency(ArrayUtilities.randomPick(FrequencyKind.values()).name());
                            return record;
                        });
                })
                .collect(Collectors.toList());

        System.out.println("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.PROVENANCE.eq("DEMO"))
                .execute();

        System.out.println("---saving record: "+records.size());
        dsl.batchInsert(records).execute();

        System.out.println("---done");


    }
}
