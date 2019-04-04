package com.khartec.waltz.data.allocation;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.AllocationType;
import com.khartec.waltz.model.allocation.ImmutableAllocation;
import com.khartec.waltz.model.allocation.MeasurablePercentage;
import com.khartec.waltz.schema.tables.records.AllocationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.Tables.ALLOCATION;

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


    public Boolean updatePercentages(EntityReference ref,
                                     long scheme,
                                     Collection<MeasurablePercentage> measurablePercentages,
                                     String username) {
        checkNotNull(ref, "Entity reference cannot be null");

        Collection<AllocationRecord> updates = CollectionUtilities.map(measurablePercentages, mp -> {
            AllocationRecord record = dsl.newRecord(ALLOCATION);

            // set the PK
            record.setEntityId(ref.id());
            record.setEntityKind(ref.kind().name());
            record.setAllocationSchemeId(scheme);
            record.setMeasurableId(mp.measurableId());

            // things that change
            record.setAllocationPercentage(mp.percentage());
            record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            record.setLastUpdatedBy(username);

            return record;
        });

        dsl.batchUpdate(updates).execute();
        return true;

    }
}


