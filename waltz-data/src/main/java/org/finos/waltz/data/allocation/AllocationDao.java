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

package org.finos.waltz.data.allocation;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation.ImmutableAllocation;
import org.finos.waltz.model.allocation.MeasurablePercentageChange;
import org.finos.waltz.schema.tables.records.AllocationRecord;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.UpdateConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;

@Repository
public class AllocationDao {

    private final DSLContext dsl;


    public static final RecordMapper<Record, Allocation> TO_DOMAIN_MAPPER = record -> {
        AllocationRecord allocationRecord = record.into(ALLOCATION);
        return ImmutableAllocation.builder()
                .id(allocationRecord.getId())
                .schemeId(allocationRecord.getAllocationSchemeId())
                .measurableRatingId(allocationRecord.getMeasurableRatingId())
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
                .select(ALLOCATION.fields())
                .from(ALLOCATION)
                .innerJoin(MEASURABLE_RATING).on(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Allocation> findByEntityAndScheme(EntityReference ref,
                                                  long schemeId){
        return dsl
                .select(ALLOCATION.fields())
                .from(ALLOCATION)
                .innerJoin(MEASURABLE_RATING).on(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId))
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(MEASURABLE_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Allocation> findByMeasurableIdAndScheme(long measurableId, long schemeId){
        return dsl
                .select(ALLOCATION.fields())
                .from(ALLOCATION)
                .innerJoin(MEASURABLE_RATING).on(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID))
                .where(MEASURABLE_RATING.MEASURABLE_ID.eq(measurableId))
                .and(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public Boolean updateAllocations(long scheme,
                                     Collection<MeasurablePercentageChange> changes,
                                     String username) {

        Map<Operation, Collection<MeasurablePercentageChange>> changesByOp = groupBy(
                MeasurablePercentageChange::operation,
                changes);

        dsl.transaction(tx -> {
            DSLContext txDsl = tx.dsl();

            Collection<MeasurablePercentageChange> toRemove = changesByOp.get(Operation.REMOVE);
            Collection<MeasurablePercentageChange> toUpdate = changesByOp.get(Operation.UPDATE);
            Collection<MeasurablePercentageChange> toInsert = changesByOp.get(Operation.ADD);

            Collection<DeleteConditionStep<AllocationRecord>> recordsToDelete = mkRemovalsFromChanges(
                    txDsl,
                    scheme,
                    toRemove);

            Collection<UpdateConditionStep<AllocationRecord>> recordsToUpdate = mkUpdatesFromChanges(
                    txDsl,
                    scheme,
                    toUpdate,
                    username);

            Collection<AllocationRecord> recordsToInsert = mkRecordsFromChanges(
                    txDsl,
                    scheme,
                    toInsert,
                    username);

            txDsl.batch(recordsToDelete)
                    .execute();

            txDsl.batch(recordsToUpdate)
                    .execute();

            txDsl.batchInsert(recordsToInsert)
                    .execute();
        });

        return true;
    }


    // -- HELPERS ----

    private static Collection<AllocationRecord> mkRecordsFromChanges(DSLContext tx,
                                                                     long scheme,
                                                                     Collection<MeasurablePercentageChange> changes,
                                                                     String username) {
        return CollectionUtilities.map(
                ListUtilities.ensureNotNull(changes),
                c -> mkRecordFromChange(tx, scheme, c, username));
    }

    private static Collection<DeleteConditionStep<AllocationRecord>> mkRemovalsFromChanges(DSLContext tx,
                                                                                           long scheme,
                                                                                           Collection<MeasurablePercentageChange> changes) {
        return CollectionUtilities.map(
                ListUtilities.ensureNotNull(changes),
                c -> mkRemovalFromChange(tx, c.measurablePercentage().measurableRatingId(), scheme));
    }

    private static Collection<UpdateConditionStep<AllocationRecord>> mkUpdatesFromChanges(DSLContext tx,
                                                                                          long scheme,
                                                                                          Collection<MeasurablePercentageChange> changes,
                                                                                          String username) {
        return CollectionUtilities.map(
                ListUtilities.ensureNotNull(changes),
                c -> mkUpdateFromChange(tx, scheme, c, username));
    }


    private static AllocationRecord mkRecordFromChange(DSLContext tx,
                                                       long scheme,
                                                       MeasurablePercentageChange c,
                                                       String username) {
        AllocationRecord record = tx.newRecord(ALLOCATION);
        record.setAllocationSchemeId(scheme);
        record.setMeasurableRatingId(c.measurablePercentage().measurableRatingId());
        record.setAllocationPercentage(c.measurablePercentage().percentage());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setProvenance("waltz");
        return record;
    }


    private static DeleteConditionStep<AllocationRecord> mkRemovalFromChange(DSLContext tx,
                                                                             long measurableRatingId,
                                                                             long scheme) {
        return tx
                .deleteFrom(ALLOCATION)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(scheme)
                        .and(ALLOCATION.MEASURABLE_RATING_ID.eq(measurableRatingId)));
    }


    private static UpdateConditionStep<AllocationRecord> mkUpdateFromChange(DSLContext tx,
                                                                            long scheme,
                                                                            MeasurablePercentageChange c,
                                                                            String username) {
        return tx
                .update(ALLOCATION)
                .set(ALLOCATION.ALLOCATION_PERCENTAGE, c.measurablePercentage().percentage())
                .set(ALLOCATION.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(ALLOCATION.LAST_UPDATED_BY, username)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(scheme)
                        .and(ALLOCATION.MEASURABLE_RATING_ID.eq(c.measurablePercentage().measurableRatingId())));

    }

    public Set<Allocation> findByMeasurableRatingId(long measurableRatingId) {
        return dsl
                .select(ALLOCATION.fields())
                .from(ALLOCATION)
                .where(ALLOCATION.MEASURABLE_RATING_ID.eq(measurableRatingId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }
}


