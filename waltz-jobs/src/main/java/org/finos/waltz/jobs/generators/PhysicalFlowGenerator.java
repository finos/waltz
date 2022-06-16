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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.Criticality;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.enum_value.EnumValueKind;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.data.physical_specification.PhysicalSpecificationDao.owningEntityNameField;
import static org.finos.waltz.schema.tables.EnumValue.ENUM_VALUE;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


public class PhysicalFlowGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static List<Criticality> criticalityDistribution = newArrayList(
            Criticality.NONE,
            Criticality.UNKNOWN,
            Criticality.LOW,
            Criticality.LOW,
            Criticality.LOW,
            Criticality.MEDIUM,
            Criticality.MEDIUM,
            Criticality.HIGH,
            Criticality.HIGH,
            Criticality.HIGH,
            Criticality.VERY_HIGH,
            Criticality.VERY_HIGH);

    private static List<EntityLifecycleStatus> lifecycleStatusDistribution = newArrayList(
            EntityLifecycleStatus.ACTIVE,
            EntityLifecycleStatus.ACTIVE,
            EntityLifecycleStatus.ACTIVE,
            EntityLifecycleStatus.PENDING);


    private static List<PhysicalFlowRecord> mkPhysicalFlowRecords(PhysicalSpecification spec,
                                                                  List<Long> logicalFlowIds,
                                                                  List<String> transportKinds) {


        return IntStream.range(0, logicalFlowIds.size() - 1)
                .mapToObj(i -> {
                    Long logicalFlowId = logicalFlowIds.remove(rnd.nextInt(logicalFlowIds.size() - 1));

                    PhysicalFlowRecord record = new PhysicalFlowRecord();
                    record.setSpecificationId(spec.id().get());
                    record.setLogicalFlowId(logicalFlowId);
                    record.setDescription("Description: " + spec + " - " + logicalFlowId.toString());
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setBasisOffset(randomPick(newArrayList(0, 0, 0, 0, 1, 1, 2, -1)));
                    record.setTransport(randomPick(transportKinds));
                    record.setFrequency(randomPick("DAILY", "MONTHLY", "WEEKLY", "ON_DEMAND"));
                    record.setCriticality(randomPick(criticalityDistribution).name());
                    record.setEntityLifecycleStatus(randomPick(lifecycleStatusDistribution).name());
                    record.setLastUpdatedBy("admin");
                    record.setCreatedBy("admin");
                    record.setCreatedAt(nowUtcTimestamp());
                    return record;
                })
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<PhysicalSpecification> specifications = dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .fetch(PhysicalSpecificationDao.TO_DOMAIN_MAPPER);

        List<String> transportKinds = dsl
                .select(ENUM_VALUE.KEY)
                .from(ENUM_VALUE)
                .where(ENUM_VALUE.TYPE.eq(EnumValueKind.TRANSPORT_KIND.dbValue()))
                .fetch(ENUM_VALUE.KEY);

        List<Tuple3<Long, Long, Long>> allLogicalFLows = dsl
                .select(
                    LOGICAL_FLOW.ID,
                    LOGICAL_FLOW.SOURCE_ENTITY_ID,
                    LOGICAL_FLOW.TARGET_ENTITY_ID)
                .from(LOGICAL_FLOW)
                .fetch(r -> Tuple.tuple(
                        r.getValue(LOGICAL_FLOW.ID),
                        r.getValue(LOGICAL_FLOW.SOURCE_ENTITY_ID),
                        r.getValue(LOGICAL_FLOW.TARGET_ENTITY_ID)));

        Map<Long, Collection<Long>> logicalFlowIdsBySourceApp = groupBy(
                t -> t.v2(),
                t -> t.v1(),
                allLogicalFLows);


        final int flowBatchSize = 100000;
        List<PhysicalFlowRecord> flowBatch = new ArrayList<PhysicalFlowRecord>((int) (flowBatchSize * 1.2));

        for (PhysicalSpecification spec : specifications) {
            Collection<Long> relatedLogicalFlowsIds = logicalFlowIdsBySourceApp.get(spec.owningEntity().id());
            if (!isEmpty(relatedLogicalFlowsIds)) {
                List<PhysicalFlowRecord> physicalFlowRecords = mkPhysicalFlowRecords(spec, new LinkedList<>(relatedLogicalFlowsIds), transportKinds);
                flowBatch.addAll(physicalFlowRecords);
            }

            if(flowBatch.size() >= flowBatchSize) {
                log(String.format("--- saving records: count: %s", flowBatch.size()));
                dsl.batchInsert(flowBatch).execute();
                flowBatch.clear();
            }
        }

        log(String.format("--- saving records: count: %s", flowBatch.size()));
        dsl.batchInsert(flowBatch).execute();
        flowBatch.clear();
        log("---done");   return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        log("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        return false;
    }
}
