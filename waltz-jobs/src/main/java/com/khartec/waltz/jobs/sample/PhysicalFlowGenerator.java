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

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.randomPick;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


public class PhysicalFlowGenerator {

    private static final Random rnd = new Random();
    private static final String provenance = "DEMO";


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<PhysicalSpecification> specifications = dsl.select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .fetch(PhysicalSpecificationDao.TO_DOMAIN_MAPPER);

        List<Tuple2<Long, EntityReference>> allLogicalFLows = dsl.select(
                LOGICAL_FLOW.ID,
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND)
                .from(LOGICAL_FLOW)
                .fetch(r -> Tuple.tuple(
                        r.getValue(LOGICAL_FLOW.ID),
                        mkRef(EntityKind.valueOf(r.getValue(LOGICAL_FLOW.SOURCE_ENTITY_KIND)),
                                r.getValue(LOGICAL_FLOW.SOURCE_ENTITY_ID))));

        Map<EntityReference, Collection<Long>> flowIdsBySource = groupBy(
                t -> t.v2(),
                t -> t.v1(),
                allLogicalFLows);

        System.out.println("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.PROVENANCE.eq(provenance))
                .execute();

        final int flowBatchSize = 100000;
        List<PhysicalFlowRecord> flowBatch = new ArrayList<PhysicalFlowRecord>((int) (flowBatchSize * 1.2));

        for (PhysicalSpecification spec : specifications) {
            Collection<Long> flowIds = flowIdsBySource.get(spec.owningEntity());
            if (!isEmpty(flowIds)) {

                List<PhysicalFlowRecord> physicalFlowRecords = mkPhysicalFlowRecords(spec, new LinkedList<>(flowIds));
                flowBatch.addAll(physicalFlowRecords);
            }

            if(flowBatch.size() >= flowBatchSize) {
                System.out.println(String.format("--- saving records: count: %s", flowBatch.size()));
                dsl.batchInsert(flowBatch).execute();
                flowBatch.clear();
            }
        }

        System.out.println(String.format("--- saving records: count: %s", flowBatch.size()));
        dsl.batchInsert(flowBatch).execute();
        flowBatch.clear();
        System.out.println("---done");
    }


    private static List<PhysicalFlowRecord> mkPhysicalFlowRecords(PhysicalSpecification spec, List<Long> flowIds) {

        return IntStream.range(0, flowIds.size() - 1)
                .mapToObj(i -> {
                    Long flowId = flowIds.remove(rnd.nextInt(flowIds.size() - 1));
                    PhysicalFlowRecord record = new PhysicalFlowRecord();
                    record.setSpecificationId(spec.id().get());
                    record.setLogicalFlowId(flowId);
                    record.setDescription("Description: " + spec + " - " + flowId.toString());
                    record.setProvenance("DEMO");
                    record.setBasisOffset(randomPick(newArrayList(0, 0, 0, 0, 1, 1, 2, -1)));
                    record.setTransport(ArrayUtilities.randomPick(TransportKind.values()).name());
                    record.setFrequency(ArrayUtilities.randomPick(FrequencyKind.values()).name());
                    record.setLastUpdatedBy("admin");
                    return record;
                })
                .collect(Collectors.toList());
    }

}
