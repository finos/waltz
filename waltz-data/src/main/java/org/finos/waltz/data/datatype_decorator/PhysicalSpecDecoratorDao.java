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

package org.finos.waltz.data.datatype_decorator;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeUsageCharacteristics;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.PhysicalSpecDataTypeRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.model.EntityKind.DATA_TYPE;
import static org.finos.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;

@Repository
public class PhysicalSpecDecoratorDao extends DataTypeDecoratorDao {

    public static final RecordMapper<? super Record, DataTypeDecorator> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecDataTypeRecord record = r.into(PHYSICAL_SPEC_DATA_TYPE);
        return ImmutableDataTypeDecorator.builder()
                .decoratorEntity(mkRef(DATA_TYPE, record.getDataTypeId()))
                .entityReference(mkRef(PHYSICAL_SPECIFICATION,record.getSpecificationId()))
                .provenance(record.getProvenance())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isReadonly(record.getIsReadonly())
                .build();
    };


    private static final Function<DataTypeDecorator, PhysicalSpecDataTypeRecord> TO_RECORD_MAPPER = sdt -> {
        PhysicalSpecDataTypeRecord r = new PhysicalSpecDataTypeRecord();
        r.setSpecificationId(sdt.entityReference().id());
        r.setDataTypeId(sdt.dataTypeId());
        r.setProvenance(sdt.provenance());
        r.setLastUpdatedAt(Timestamp.valueOf(sdt.lastUpdatedAt()));
        r.setLastUpdatedBy(sdt.lastUpdatedBy());
        r.setIsReadonly(sdt.isReadonly());
        return r;
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    @Override
    public DataTypeDecorator getByEntityIdAndDataTypeId(long specId, long dataTypeId) {
        return dsl
                .select(PHYSICAL_SPEC_DATA_TYPE.fields())
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(specId))
                .and(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(dataTypeId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    @Override
    public List<DataTypeDecorator> findByEntityId(long specId) {
        return dsl
                .select(PHYSICAL_SPEC_DATA_TYPE.fields())
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(specId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Override
    public List<DataTypeDecorator> findByEntityIdSelector(Select<Record1<Long>> specIdSelector,
                                                          Optional<EntityKind> entityKind) {
        return dsl
                .select(PHYSICAL_SPEC_DATA_TYPE.fields())
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.in(specIdSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Override
    public List<DataTypeDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        throw new UnsupportedOperationException("method not supported for " + PHYSICAL_SPECIFICATION.prettyName());
    }

    @Override
    public List<DataTypeDecorator> findByFlowIdSelector(Select<Record1<Long>> flowIdSelector) {
        return null;
    }


    @Override
    public List<DataTypeDecorator> findByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector) {
        throw new UnsupportedOperationException("method not supported for " + PHYSICAL_SPECIFICATION.prettyName());
    }


    @Override
    public List<DataTypeDecorator> findByFlowIds(Collection<Long> flowIds) {
        throw new UnsupportedOperationException("method not supported for " + PHYSICAL_SPECIFICATION.prettyName());
    }


    @Override
    public int[] addDecorators(Collection<DataTypeDecorator> specificationDataTypes) {
        checkNotNull(specificationDataTypes, "specificationDataTypes cannot be null");

        List<PhysicalSpecDataTypeRecord> records = specificationDataTypes.stream()
                .map(TO_RECORD_MAPPER)
                .collect(toList());

        return dsl.batchInsert(records)
                .execute();
    }


    @Override
    public int removeDataTypes(EntityReference associatedEntityRef, Collection<Long> dataTypeIds) {
        return dsl
                .deleteFrom(PHYSICAL_SPEC_DATA_TYPE)
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(associatedEntityRef.id()))
                .and(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.in(dataTypeIds))
                        .and(PHYSICAL_SPEC_DATA_TYPE.IS_READONLY.isFalse())
                .execute();
    }


    @Override
    public List<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristics(EntityReference ref) {

        Field<Integer> numberOfFlowsSharingDatatype = DSL.countDistinct(Tables.PHYSICAL_FLOW.ID).as("numberOfFlowsSharingDatatype");

        return dsl
                .select(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID, PHYSICAL_SPEC_DATA_TYPE.IS_READONLY, numberOfFlowsSharingDatatype)
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .leftJoin(Tables.PHYSICAL_FLOW).on(Tables.PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID)
                        .and(Tables.PHYSICAL_FLOW.IS_REMOVED.isFalse())
                        .and(Tables.PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())))
                .where(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(ref.id()))
                .groupBy(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID, PHYSICAL_SPEC_DATA_TYPE.IS_READONLY)
                .fetch(r -> {
                    Integer usageCount = r.get(numberOfFlowsSharingDatatype);
                    return ImmutableDataTypeUsageCharacteristics.builder()
                            .dataTypeId(r.get(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID))
                            .warningMessageForEditors(calcWarningMessageForEditors(usageCount))
                            .warningMessageForViewers(calcWarningMessageForViewers(usageCount))
                            .isRemovable(true)
                            .build();
                });
    }


    private String calcWarningMessageForEditors(int usageCount) {
        if (usageCount == 0) {
            return "This spec has no implementing flows.";
        } else if (usageCount > 1) {
            return format(
                    "Caution, this spec is used by multiple physical flows. There are %d usages.",
                    usageCount);
        } else {
            return null;
        }
    }


    private String calcWarningMessageForViewers(int usageCount) {
        if (usageCount == 0) {
            return "This spec has no implementing flows.";
        } else {
            return null;
        }
    }


    public int rippleDataTypesToLogicalFlows() {
        SelectConditionStep<Record7<Long, String, Long, String, String, Timestamp, String>> qry = DSL
                .selectDistinct(
                        PHYSICAL_FLOW.LOGICAL_FLOW_ID,
                        DSL.val(DATA_TYPE.name()),
                        PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID,
                        DSL.val(AuthoritativenessRatingValue.NO_OPINION.value()),
                        DSL.val("waltz"),
                        DSL.val(Timestamp.valueOf(nowUtc())),
                        DSL.val("admin"))
                .from(PHYSICAL_SPEC_DATA_TYPE)
                .innerJoin(Tables.DATA_TYPE)
                .on(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(Tables.DATA_TYPE.ID)
                        .and(Tables.DATA_TYPE.UNKNOWN.isFalse()
                                .and(Tables.DATA_TYPE.DEPRECATED.isFalse())))
                .innerJoin(Tables.PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(Tables.PHYSICAL_SPECIFICATION.ID)
                        .and(Tables.PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse()))
                .innerJoin(PHYSICAL_FLOW)
                .on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID)
                        .and(PHYSICAL_FLOW.IS_REMOVED.isFalse()
                                .and(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))))
                .innerJoin(Tables.LOGICAL_FLOW)
                .on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(Tables.LOGICAL_FLOW.ID)
                        .and(Tables.LOGICAL_FLOW.IS_REMOVED.isFalse()
                                .and(Tables.LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))))
                .leftJoin(LOGICAL_FLOW_DECORATOR)
                .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(Tables.LOGICAL_FLOW.ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID))
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name())))
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.isNull());

        return dsl
                .insertInto(LOGICAL_FLOW_DECORATOR)
                .columns(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID,
                        LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
                        LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                        LOGICAL_FLOW_DECORATOR.RATING,
                        LOGICAL_FLOW_DECORATOR.PROVENANCE,
                        LOGICAL_FLOW_DECORATOR.LAST_UPDATED_AT,
                        LOGICAL_FLOW_DECORATOR.LAST_UPDATED_BY)
                .select(qry)
                .execute();
    }
}
