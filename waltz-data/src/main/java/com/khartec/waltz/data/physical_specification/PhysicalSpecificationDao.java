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

package com.khartec.waltz.data.physical_specification;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
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
                .build();
    };


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
                .and(NOT_REMOVED);

        Condition isTargetCondition = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()))
                .and(NOT_REMOVED);

        return dsl.select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(isOwnerCondition)
                .or(isTargetCondition)
                .or(isSourceCondition)
                .fetch(TO_DOMAIN_MAPPER)
                .stream()
                .collect(Collectors.toSet());
    }


    public PhysicalSpecification getById(long id) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<PhysicalSpecification> findBySelector(Select<Record1<Long>> selector) {
        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public PhysicalSpecification getByParsedFlow(PhysicalFlowParsed flow) {

        Condition condition = PHYSICAL_SPECIFICATION.NAME.eq(flow.name())
                        .and(PHYSICAL_SPECIFICATION.FORMAT.eq(flow.format().name()))
                        .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(flow.owner().kind().name()))
                        .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(flow.owner().id()))
                        .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse());

        return dsl
                .select(PHYSICAL_SPECIFICATION.fields())
                .select(owningEntityNameField)
                .from(PHYSICAL_SPECIFICATION)
                .where(condition)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public boolean isUsed(long id) {
        Field<Boolean> specUsed = DSL.when(
                    exists(selectFrom(PHYSICAL_FLOW).where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(id))),
                    val(true))
                .otherwise(false).as("spec_used");

        return dsl.select(specUsed)
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

        record.store();
        return record.getId();
    }


    public int delete(long specId) {
        return dsl.deleteFrom(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.ID.eq(specId))
                .and(notExists(selectFrom(PHYSICAL_FLOW)
                                .where(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specId))))
                .execute();
    }

}
