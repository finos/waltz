package com.khartec.waltz.data.allocation;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.AllocationType;
import com.khartec.waltz.model.allocation.ImmutableAllocation;
import com.khartec.waltz.schema.tables.records.AllocationRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
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
                .type(allocationRecord.getIsFixed()
                        ? AllocationType.FIXED
                        : AllocationType.FLOATING)
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


    private SelectConditionStep<Record3<Long, Long, String>> findMeasurableRatingsBySchemeId(long schemeId){
        return dsl
                .select(MEASURABLE.ID,
                        MEASURABLE_RATING.ENTITY_ID,
                        MEASURABLE_RATING.ENTITY_KIND)
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE.ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .innerJoin(ALLOCATION_SCHEME).on(MEASURABLE_CATEGORY.ID.eq(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID))
                .where(ALLOCATION_SCHEME.ID.eq(schemeId));
    }


    public Collection<Allocation> findAllocationsToRemove(long schemeId) {

        Collection<Record3<Long, Long, String>> allocationsToRemove = findAllocationsBySchemeId(schemeId)
                .except(findMeasurableRatingsBySchemeId(schemeId))
                .fetch();

        return allocationsToRemove
                .stream()
                .flatMap(r ->
                        dsl
                                .selectFrom(ALLOCATION)
                                .where(ALLOCATION.MEASURABLE_ID.eq(r.value1()))
                                .and(ALLOCATION.ENTITY_ID.eq(r.value2()))
                                .and(ALLOCATION.ENTITY_KIND.eq(r.value3()))
                                .and(ALLOCATION.ALLOCATION_SCHEME_ID.eq(schemeId))
                                .fetch(TO_DOMAIN_MAPPER)
                                .stream())

                .collect(Collectors.toSet());
    }


    public boolean removeAllocations(Collection<Allocation> allocations){

        checkNotNull(allocations, "must have allocations to remove");

        long inputCount = allocations.size();

        long countDelete = allocations.stream().map(r -> dsl
                .deleteFrom(ALLOCATION)
                .where(ALLOCATION.MEASURABLE_ID.eq(r.measurableId()))
                .and(ALLOCATION.ENTITY_ID.eq(r.entityReference().id()))
                .and(ALLOCATION.ENTITY_KIND.eq(r.entityReference().kind().name()))
                .execute())
                .count();

        return (countDelete == inputCount);

    }


    public Collection<Record3<Long, Long, String>> addMissingAllocations(long schemeId){

        Collection<Record3<Long, Long, String>> allocationsToAdd = findMeasurableRatingsBySchemeId(schemeId)
                .except(findAllocationsBySchemeId(schemeId))
                .fetch();

        return allocationsToAdd;

    }


    public Collection<AllocationRecord> addAllocations(Collection<Record3<Long, Long, String>> measurableRatings, long schemeId){

        Collection<AllocationRecord> newAllocationsToAdd = CollectionUtilities.map(measurableRatings, mr -> {

            AllocationRecord record = dsl.newRecord(ALLOCATION);

            record.setEntityId(mr.value2());
            record.setEntityKind(mr.value3());
            record.setMeasurableId(mr.value1());
            record.setAllocationSchemeId(schemeId);

            record.setAllocationPercentage(BigDecimal.ZERO);
            record.setIsFixed(false);
            record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            record.setLastUpdatedBy("admin");
            record.setProvenance("WALTZ");

            return record;
        });

        dsl.batchInsert(newAllocationsToAdd).execute();

        return newAllocationsToAdd;

    }


    public Boolean updateType(EntityReference ref,
                              long scheme,
                              long measurable,
                              AllocationType type,
                              String username) {
        checkNotNull(ref, "ref cannot be null");

        int updateCount = dsl
                .update(ALLOCATION)
                .set(ALLOCATION.IS_FIXED, type == AllocationType.FIXED)
                .set(ALLOCATION.LAST_UPDATED_BY, username)
                .set(ALLOCATION.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(scheme))
                .and(ALLOCATION.ENTITY_KIND.eq(ref.kind().name()))
                .and(ALLOCATION.ENTITY_ID.eq(ref.id()))
                .and(ALLOCATION.MEASURABLE_ID.eq(measurable))
                .and(ALLOCATION.IS_FIXED.eq(type != AllocationType.FIXED))
                .execute();
        return updateCount == 1;
    }

    public Boolean updatePercentage(EntityReference ref,
                                    long scheme,
                                    long measurable,
                                    BigDecimal percentage,
                                    String username) {
        checkNotNull(ref, "Entity reference cannot be null");

        int updateCount = dsl.update(ALLOCATION)
                .set(ALLOCATION.ALLOCATION_PERCENTAGE, percentage)
                .set(ALLOCATION.LAST_UPDATED_BY, username)
                .set(ALLOCATION.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(scheme))
                .and(ALLOCATION.ENTITY_KIND.eq(ref.kind().name()))
                .and(ALLOCATION.ENTITY_ID.eq(ref.id()))
                .and(ALLOCATION.MEASURABLE_ID.eq(measurable))
                .and(ALLOCATION.IS_FIXED.isTrue())
                .execute();
        return updateCount == 1;
    }


    public Boolean updateAllocations(Collection<Allocation> allocations,
                                          String username) {

        Collection<AllocationRecord> updates = CollectionUtilities.map(allocations, alloc -> {
            AllocationRecord record = dsl.newRecord(ALLOCATION);

            // set the PK
            record.setEntityId(alloc.entityReference().id());
            record.setEntityKind(alloc.entityReference().kind().name());
            record.setAllocationSchemeId(alloc.schemeId());
            record.setMeasurableId(alloc.measurableId());

            // things that change
            record.setAllocationPercentage(alloc.percentage());
            record.setIsFixed(alloc.type().equals(AllocationType.FIXED));
            record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            record.setLastUpdatedBy(username);

            return record;
        });

        dsl.batchUpdate(updates).execute();
        return true;

    }
}


