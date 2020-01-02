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

package com.khartec.waltz.data.allocation;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.ImmutableAllocation;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import com.khartec.waltz.schema.tables.records.AllocationRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.Tables.*;

@Repository
public class AllocationDao {

    private final DSLContext dsl;


    public static final RecordMapper<Record, Allocation> TO_DOMAIN_MAPPER = record -> {
        AllocationRecord allocationRecord = record.into(ALLOCATION);
        return ImmutableAllocation.builder()
                .schemeId(allocationRecord.getAllocationSchemeId())
                .measurableId(allocationRecord.getMeasurableId())
                .entityReference(readRef(allocationRecord, ALLOCATION.ENTITY_KIND, ALLOCATION.ENTITY_ID))
                .percentage(allocationRecord.getAllocationPercentage())
                .lastUpdatedAt(allocationRecord.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(allocationRecord.getLastUpdatedBy())
                .externalId(Optional.ofNullable(allocationRecord.getExternalId()))
                .provenance(allocationRecord.getProvenance())
                .build();

    };


    @Autowired
    public AllocationDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Allocation> findByEntity(EntityReference ref) {
        return dsl
                .selectFrom(ALLOCATION)
                .where(ALLOCATION.ENTITY_KIND.eq(ref.kind().name()))
                .and(ALLOCATION.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Allocation> findByEntityAndScheme(EntityReference ref,
                                                  long schemeId){
        return dsl
                .selectFrom(ALLOCATION)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId))
                .and(ALLOCATION.ENTITY_KIND.eq(ref.kind().name()))
                .and(ALLOCATION.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Allocation> findByMeasurableIdAndScheme(long measurableId, long schemeId){
        return dsl
                .selectFrom(ALLOCATION)
                .where(ALLOCATION.MEASURABLE_ID.eq(measurableId))
                .and(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private SelectConditionStep<Record3<Long, Long, String>> findAllocationsBySchemeId(long schemeId){
        return dsl
                .select(ALLOCATION.MEASURABLE_ID,
                        ALLOCATION.ENTITY_ID,
                        ALLOCATION.ENTITY_KIND)
                .from(ALLOCATION)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId));
    }


    public Boolean updateAllocations(EntityReference ref,
                                     long scheme,
                                     Collection<MeasurablePercentageChange> changes,
                                     String username) {

        Map<Operation, Collection<MeasurablePercentageChange>> changesByOp = groupBy(
                MeasurablePercentageChange::operation,
                changes);

        dsl.transaction(tx -> {
            DSLContext txDsl = tx.dsl();

            Collection<AllocationRecord> recordsToDelete = mkRecordsFromChanges(
                    ref,
                    scheme,
                    changesByOp.get(Operation.REMOVE),
                    username);

            Collection<AllocationRecord> recordsToUpdate = mkRecordsFromChanges(
                    ref,
                    scheme,
                    changesByOp.get(Operation.UPDATE),
                    username);

            Collection<AllocationRecord> recordsToInsert = mkRecordsFromChanges(
                    ref,
                    scheme,
                    changesByOp.get(Operation.ADD),
                    username);

            txDsl.batchDelete(recordsToDelete)
                    .execute();
            txDsl.batchUpdate(recordsToUpdate)
                    .execute();
            txDsl.batchInsert(recordsToInsert)
                    .execute();
        });

        return true;
    }


    // -- HELPERS ----

    private static Collection<AllocationRecord> mkRecordsFromChanges(EntityReference ref,
                                                                                 long scheme,
                                                                                 Collection<MeasurablePercentageChange> changes,
                                                                                 String username) {
        return CollectionUtilities.map(
                ListUtilities.ensureNotNull(changes),
                c -> mkRecordFromChange(ref, scheme, c, username));
    }


    private static AllocationRecord mkRecordFromChange(EntityReference ref, long scheme, MeasurablePercentageChange c, String username) {
        AllocationRecord record = new AllocationRecord();
        record.setAllocationSchemeId(scheme);
        record.setEntityId(ref.id());
        record.setEntityKind(ref.kind().name());
        record.setMeasurableId(c.measurablePercentage().measurableId());
        record.setAllocationPercentage(c.measurablePercentage().percentage());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setProvenance("waltz");
        return record;
    }

}


