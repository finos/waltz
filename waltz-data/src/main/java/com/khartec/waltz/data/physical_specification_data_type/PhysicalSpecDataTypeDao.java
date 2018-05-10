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

import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.data_flow_decorator.ImmutableLogicalFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.physical_specification_data_type.ImmutablePhysicalSpecificationDataType;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.PhysicalSpecDataTypeRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
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
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;


    @Autowired
    public PhysicalSpecDataTypeDao(DSLContext dsl,
                                   LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");

        this.dsl = dsl;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
    }


    public PhysicalSpecificationDataType getBySpecIdAndDataTypeID(long specId, long dataTypeId) {
        return dsl.selectFrom(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(specId))
                .and(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(dataTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
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


    public int[] rippleDataTypesToLogicalFlows() {
        // get distinct set of specs and data types
        // for each spec -> find physical flows -> logical flows
        List<LogicalFlowDecorator> unratedFlowDecorators = dsl
                .selectDistinct(PHYSICAL_FLOW.LOGICAL_FLOW_ID, PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID)
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .join(PHYSICAL_FLOW)
                .on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .fetch()
                .stream()
                .map(r -> ImmutableLogicalFlowDecorator.builder()
                        .rating(AuthoritativenessRating.NO_OPINION)
                        .provenance("waltz")
                        .dataFlowId(r.value1())
                        .decoratorEntity(EntityReference.mkRef(EntityKind.DATA_TYPE, r.value1()))
                        .lastUpdatedBy("admin")
                        .lastUpdatedAt(nowUtc())
                        .build()
                )
                .collect(toList());

        // for each logical flow - add data types
        return logicalFlowDecoratorDao.addDecorators(unratedFlowDecorators);
    }

}
