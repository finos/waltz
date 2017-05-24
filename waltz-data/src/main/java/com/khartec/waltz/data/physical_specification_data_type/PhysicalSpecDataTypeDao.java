package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.physical_specification_data_type.ImmutablePhysicalSpecificationDataType;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.schema.tables.records.PhysicalSpecDataTypeRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;

@Repository
public class PhysicalSpecDataTypeDao {

    public static final RecordMapper<? super Record, PhysicalSpecificationDataType> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecDataTypeRecord record = r.into(PHYSICAL_SPEC_DATA_TYPE);
        return ImmutablePhysicalSpecificationDataType.builder()
                .dataTypeId(record.getDataTypeId())
                .specificationId(record.getSpecificationId())
                .provenance(record.getProvenance())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecDataTypeDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<PhysicalSpecificationDataType> findBySpecificationId(long specId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(specId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecificationDataType> findBySpecificationIdSelector(Select<Record1<Long>> specIdSelector) {
        return dsl.selectFrom(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.in(specIdSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
