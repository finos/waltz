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

package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.physical_flow.PhysicalFlowParsed;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.schema.tables.records.PhysicalSpecificationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static com.khartec.waltz.data.physical_flow.PhysicalFlowDao.PHYSICAL_FLOW_NOT_REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.impl.DSL.*;

@Repository
public class PhysicalSpecificationDao {


    public static final Field<String> owningEntityNameField = InlineSelectFieldFactory.mkNameField(
                PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID,
                PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR))
            .as("owning_name_field");


    public static final RecordMapper<? super Record, PhysicalSpecification> TO_DOMAIN_MAPPER = r -> {
        PhysicalSpecificationRecord record = r.into(PHYSICAL_SPECIFICATION);
        return ImmutablePhysicalSpecification.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .owningEntity(mkRef(
                        EntityKind.valueOf(record.getOwningEntityKind()),
                        record.getOwningEntityId(),
                        r.getValue(owningEntityNameField)))
                .name(record.getName())
                .description(record.getDescription())
                .format(DataFormatKind.valueOf(record.getFormat()))
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .isRemoved(record.getIsRemoved())
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .build();
    };

    public static final Condition PHYSICAL_SPEC_NOT_REMOVED = PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse();


    private final DSLContext dsl;


    @Autowired
    public PhysicalSpecificationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Set<PhysicalSpecification> findByEntityReference(EntityReference ref) {

        Condition isOwnerCondition = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));

        Condition isSourceCondition = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()))
                .and(LOGICAL_NOT_REMOVED);

        Condition isTargetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()))
                .and(LOGICAL_NOT_REMOVED);

        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(isOwnerCondition)
                .or(isTargetCondition)
                .or(isSourceCondition)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Collection<PhysicalSpecification> findByIds(Collection<Long> ids) {
        return basicSelectByCondition(PHYSICAL_SPECIFICATION.ID.in(ids))
                .fetch(TO_DOMAIN_MAPPER);
    }



    public PhysicalSpecification getById(long id) {
        return basicSelectByCondition(PHYSICAL_SPECIFICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalSpecification> findBySelector(Select<Record1<Long>> selector) {
        return basicSelectByCondition(PHYSICAL_SPECIFICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public PhysicalSpecification getByParsedFlow(PhysicalFlowParsed flow) {

        Condition condition = PHYSICAL_SPECIFICATION.NAME.eq(flow.name())
                        .and(PHYSICAL_SPECIFICATION.FORMAT.eq(flow.format().name()))
                        .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(flow.owner().kind().name()))
                        .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(flow.owner().id()))
                        .and(PHYSICAL_SPEC_NOT_REMOVED);

        return basicSelectByCondition(condition)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public boolean isUsed(long id) {
        Field<Boolean> specUsed = DSL.when(
                    exists(selectFrom(PHYSICAL_FLOW)
                            .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(id))
                            .and(PHYSICAL_FLOW_NOT_REMOVED)),
                    val(true))
                .otherwise(false).as("spec_used");

        return dsl
                .select(specUsed)
                .fetchOne(specUsed);

    }


    public Long create(PhysicalSpecification specification) {
        checkNotNull(specification, "specification cannot be null");
        checkFalse(specification.id().isPresent(), "specification must not have an id");

        PhysicalSpecificationRecord record = dsl.newRecord(PHYSICAL_SPECIFICATION);
        record.setOwningEntityKind(specification.owningEntity().kind().name());
        record.setOwningEntityId(specification.owningEntity().id());

        record.setName(specification.name());
        record.setExternalId(specification.externalId().orElse(""));
        record.setDescription(specification.description());
        record.setFormat(specification.format().name());
        record.setLastUpdatedAt(Timestamp.valueOf(specification.lastUpdatedAt()));
        record.setLastUpdatedBy(specification.lastUpdatedBy());
        record.setIsRemoved(specification.isRemoved());
        record.setProvenance("waltz");

        record.setCreatedAt(specification.created().get().atTimestamp());
        record.setCreatedBy(specification.created().get().by());

        record.store();
        return record.getId();
    }


    public int markRemovedIfUnused(long specId) {
        return dsl.update(PHYSICAL_SPECIFICATION)
                .set(PHYSICAL_SPECIFICATION.IS_REMOVED, true)
                .where(PHYSICAL_SPECIFICATION.ID.eq(specId))
                .and(notExists(selectFrom(PHYSICAL_FLOW)
                        .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specId))
                        .and(PHYSICAL_FLOW_NOT_REMOVED))
                )
                .execute();
    }

    public int updateExternalId(long specificationId, String externalId) {
        return dsl.update(PHYSICAL_SPECIFICATION)
                .set(PHYSICAL_SPECIFICATION.EXTERNAL_ID, externalId)
                .where(PHYSICAL_SPECIFICATION.ID.eq(specificationId))
                .execute();
    }


    private SelectConditionStep<Record> basicSelectByCondition(Condition in) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(in);
    }

    public int makeActive(Long specificationId) {
        return dsl.update(PHYSICAL_SPECIFICATION)
                .set(PHYSICAL_SPECIFICATION.IS_REMOVED, false)
                .where(PHYSICAL_SPECIFICATION.ID.eq(specificationId))
                .execute();
    }
}
