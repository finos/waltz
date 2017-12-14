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

package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.physical_specification_data_type.ImmutablePhysicalSpecificationDataType;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.schema.tables.records.PhysicalSpecDataTypeRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;
import static java.util.stream.Collectors.toList;

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


    private static final Function<PhysicalSpecificationDataType, PhysicalSpecDataTypeRecord> TO_RECORD_MAPPER = sdt -> {
        PhysicalSpecDataTypeRecord r = new PhysicalSpecDataTypeRecord();
        r.setSpecificationId(sdt.specificationId());
        r.setDataTypeId(sdt.dataTypeId());
        r.setProvenance(sdt.provenance());
        r.setLastUpdatedAt(Timestamp.valueOf(sdt.lastUpdatedAt()));
        r.setLastUpdatedBy(sdt.lastUpdatedBy());
        return r;
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


    public int[] addDataTypes(Collection<PhysicalSpecificationDataType> specificationDataTypes) {
        checkNotNull(specificationDataTypes, "specificationDataTypes cannot be null");

        List<PhysicalSpecDataTypeRecord> records = specificationDataTypes.stream()
                .map(TO_RECORD_MAPPER)
                .collect(toList());

        return dsl.batchInsert(records)
                .execute();
    }


    public int[] removeDataTypes(Collection<PhysicalSpecificationDataType> specificationDataTypes) {
        checkNotNull(specificationDataTypes, "specificationDataTypes cannot be null");

        List<PhysicalSpecDataTypeRecord> records = specificationDataTypes.stream()
                .map(TO_RECORD_MAPPER)
                .collect(toList());

        return dsl.batchDelete(records)
                .execute();
    }
}
