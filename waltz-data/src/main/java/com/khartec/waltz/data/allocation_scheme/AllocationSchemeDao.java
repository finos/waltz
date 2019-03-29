package com.khartec.waltz.data.allocation_scheme;

import com.khartec.waltz.model.allocation_scheme.AllocationScheme;
import com.khartec.waltz.model.allocation_scheme.ImmutableAllocationScheme;
import com.khartec.waltz.schema.tables.records.AllocationSchemeRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.AllocationScheme.ALLOCATION_SCHEME;

@Repository
public class AllocationSchemeDao {

    private final DSLContext dsl;

    public static final RecordMapper<Record, AllocationScheme> TO_DOMAIN_MAPPER = record -> {
        AllocationSchemeRecord allocationSchemeRecord = record.into(ALLOCATION_SCHEME);
        return ImmutableAllocationScheme.builder()
                .id(allocationSchemeRecord.getId())
                .name(allocationSchemeRecord.getName())
                .description(allocationSchemeRecord.getDescription())
                .measurableCategoryId(allocationSchemeRecord.getMeasurableCategoryId())
                .build();

    };

    @Autowired
    public AllocationSchemeDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public AllocationScheme getById(long id){
        return dsl
                .selectFrom(ALLOCATION_SCHEME)
                .where(ALLOCATION_SCHEME.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<AllocationScheme> findAll(){
        return dsl
                .selectFrom(ALLOCATION_SCHEME)
                .fetch(TO_DOMAIN_MAPPER);
    }

    public List<AllocationScheme> findByCategoryId(long categoryId){
        return dsl
                .selectFrom(ALLOCATION_SCHEME)
                .where(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
