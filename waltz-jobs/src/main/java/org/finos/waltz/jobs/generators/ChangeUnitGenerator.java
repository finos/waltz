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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.change_unit.ChangeUnitDao;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.change_unit.ChangeAction;
import org.finos.waltz.model.change_unit.ChangeUnit;
import org.finos.waltz.model.change_unit.ExecutionStatus;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.schema.tables.records.AttributeChangeRecord;
import org.finos.waltz.schema.tables.records.ChangeUnitRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.RandomUtilities.*;
import static org.finos.waltz.common.StringUtilities.joinUsing;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.ChangeSet.CHANGE_SET;
import static org.finos.waltz.schema.tables.ChangeUnit.CHANGE_UNIT;

public class ChangeUnitGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        LocalDateTime now = LocalDateTime.now();

        List<Long> changeSetIds = dsl
                .select(CHANGE_SET.ID)
                .from(CHANGE_SET)
                .fetch(CHANGE_SET.ID);

        List<PhysicalFlow> physicalFlows = dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .fetch(PhysicalFlowDao.TO_DOMAIN_MAPPER);

        AtomicInteger counter = new AtomicInteger(0);

        List<ChangeUnitRecord> groupRecords = changeSetIds
                .stream()
                .flatMap(id -> randomlySizedIntStream(0, 5)
                        .mapToObj(idx -> randomPick(physicalFlows))
                        .distinct()
                        .map(flow -> {
                            ChangeUnitRecord record = dsl.newRecord(CHANGE_UNIT);
                            record.setChangeSetId(id);
                            record.setSubjectEntityKind(EntityKind.PHYSICAL_FLOW.name());
                            record.setSubjectEntityId(flow.id().get());
                            record.setSubjectInitialStatus(flow.entityLifecycleStatus().name());
                            record.setExecutionStatus(ExecutionStatus.PENDING.name());
                            record.setLastUpdatedAt(Timestamp.valueOf(now));
                            record.setLastUpdatedBy("admin");
                            record.setExternalId(String.format("change-unit-ext-%s", counter.addAndGet(1)));
                            record.setProvenance(SAMPLE_DATA_PROVENANCE);


                            // if flow pending -> activate, activating flow, desc
                            // if flow active -> retire or modify
                            // if modify -> create attribute changes
                            ChangeAction action = mkChangeAction(flow);
                            record.setAction(action.name());
                            record.setName(mkName(flow, action));
                            record.setDescription("Description: " + mkName(flow, action));

                            return record;
                        }))
                .collect(toList());

        dsl.batchStore(groupRecords).execute();


        List<AttributeChangeRecord> attributeChangeRecords = mkAttributeChanges(dsl, physicalFlows);
        dsl.batchStore(attributeChangeRecords).execute();


        return null;

    }


    private List<AttributeChangeRecord> mkAttributeChanges(DSLContext dsl, List<PhysicalFlow> physicalFlows) {
        List<ChangeUnit> modifyCUs = dsl
                .selectFrom(CHANGE_UNIT)
                .where(CHANGE_UNIT.ACTION.eq(ChangeAction.MODIFY.name()))
                .fetch(ChangeUnitDao.TO_DOMAIN_MAPPER);

        List<String> attributes = ListUtilities.asList("criticality", "frequency", "DataType");

        Map<Long, PhysicalFlow> flowsById = MapUtilities.indexBy(f -> f.id().get(), physicalFlows);

        List<AttributeChangeRecord> attributeChanges = modifyCUs
                .stream()
                .flatMap(cu -> randomlySizedIntStream(1, 2)
                        .mapToObj(idx -> randomPick(attributes))
                        .map(attribute -> {
                            PhysicalFlow flow = flowsById.get(cu.subjectEntity().id());

                            switch (attribute) {
                                case "criticality":
                                    return mkCriticalityChange(dsl, cu, flow, attribute);
                                case "frequency":
                                    return mkFrequencyChange(dsl, cu, flow, attribute);
                                case "DataType":
                                    return mkDataTypeChange(dsl, cu, flow, attribute);
                                default:
                                    throw new UnsupportedOperationException("Attribute change not supported: " + attribute);
                            }
                        }))
                .collect(toList());

        return attributeChanges;
    }


    private AttributeChangeRecord mkDataTypeChange(DSLContext dsl, ChangeUnit cu, PhysicalFlow flow, String name) {
        List<DataType> allDataTypes = dsl.selectFrom(DATA_TYPE)
                .fetch(DataTypeDao.TO_DOMAIN);

        List<DataType> newDataTypes = randomPick(allDataTypes, randomIntBetween(1, 5));
        String json = "["
                + joinUsing(newDataTypes, d -> String.format("{\"dataTypeId\": %s}", d.id().get()), ",")
                + "]";

        AttributeChangeRecord record = dsl.newRecord(ATTRIBUTE_CHANGE);
        record.setChangeUnitId(cu.id().get());
        record.setType("json");
        record.setOldValue("[]");
        record.setNewValue(json);
        record.setName(name);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setProvenance(SAMPLE_DATA_PROVENANCE);
        return record;
    }


    private AttributeChangeRecord mkFrequencyChange(DSLContext dsl, ChangeUnit cu, PhysicalFlow flow, String name) {
        AttributeChangeRecord record = dsl.newRecord(ATTRIBUTE_CHANGE);
        record.setChangeUnitId(cu.id().get());
        record.setType("string");
        record.setOldValue(flow.frequency().value());
        record.setNewValue(randomPick("DAILY", "MONTHLY", "WEEKLY", "ON_DEMAND"));
        record.setName(name);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setProvenance(SAMPLE_DATA_PROVENANCE);
        return record;
    }


    private AttributeChangeRecord mkCriticalityChange(DSLContext dsl, ChangeUnit cu, PhysicalFlow flow, String name) {
        AttributeChangeRecord record = dsl.newRecord(ATTRIBUTE_CHANGE);
        record.setChangeUnitId(cu.id().get());
        record.setType("string");
        record.setOldValue(flow.criticality().value());
        record.setNewValue(randomPick("LOW", "MEDIUM", "HIGH"));
        record.setName(name);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setProvenance(SAMPLE_DATA_PROVENANCE);
        return record;
    }


    private String mkName(PhysicalFlow flow, ChangeAction action) {
        return StringUtilities.lower(String.format("%s physical flow with id: %s ", action, flow.id().get()));
    }


    private ChangeAction mkChangeAction(PhysicalFlow flow) {
        if(flow.entityLifecycleStatus().equals(EntityLifecycleStatus.PENDING)) {
            return ChangeAction.ACTIVATE;
        }
        return randomPick(ChangeAction.MODIFY, ChangeAction.RETIRE);
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(CHANGE_UNIT)
           .where(CHANGE_UNIT.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
           .execute();

        dsl.deleteFrom(ATTRIBUTE_CHANGE)
                .where(ATTRIBUTE_CHANGE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        return true;
    }
}
