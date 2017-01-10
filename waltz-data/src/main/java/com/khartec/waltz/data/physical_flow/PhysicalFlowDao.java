/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.EntityNameUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowLineage.PHYSICAL_FLOW_LINEAGE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.selectFrom;


@Repository
public class PhysicalFlowDao {


    public static final Field<String> targetEntityNameField = EntityNameUtilities.mkEntityNameField(
            PHYSICAL_FLOW.TARGET_ENTITY_ID,
            PHYSICAL_FLOW.TARGET_ENTITY_KIND,
            ListUtilities.newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR)).as("target_name_field");


    public static final RecordMapper<Record, PhysicalFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowRecord record = r.into(PHYSICAL_FLOW);
        return ImmutablePhysicalFlow.builder()
                .id(record.getId())
                .provenance(record.getProvenance())
                .specificationId(record.getSpecificationId())
                .basisOffset(record.getBasisOffset())
                .frequency(FrequencyKind.valueOf(record.getFrequency()))
                .description(record.getDescription())
                .target(
                        EntityReference.mkRef(
                                EntityKind.valueOf(record.getTargetEntityKind()),
                                record.getTargetEntityId(),
                                r.getValue(targetEntityNameField)))
                .transport(TransportKind.valueOf(record.getTransport()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        Select<Record> consumedFlows = findByConsumerEntityReferenceQuery(ref);
        Select<Record> producedFlows = findByProducerEntityReferenceQuery(ref);

        return consumedFlows
                .unionAll(producedFlows)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findByProducer(EntityReference ref) {
        return findByProducerEntityReferenceQuery(ref)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findByConsumer(EntityReference ref) {
        return findByConsumerEntityReferenceQuery(ref)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public PhysicalFlow getById(long id) {
        return findByCondition(PHYSICAL_FLOW.ID.eq(id))
                .stream()
                .findFirst()
                .orElse(null);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return findByCondition(PHYSICAL_FLOW.SPECIFICATION_ID.eq(specificationId));
    }


    public List<PhysicalFlow> findBySelector(Select<Record1<Long>> selector) {
        return findByCondition(PHYSICAL_FLOW.ID.in(selector));
    }


    public int delete(long flowId) {
        return dsl.delete(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .and(notExists(selectFrom(PHYSICAL_FLOW_LINEAGE)
                        .where(PHYSICAL_FLOW_LINEAGE.CONTRIBUTOR_FLOW_ID.eq(flowId))))
                .execute();
    }


    private List<PhysicalFlow> findByCondition(Condition condition) {
        return dsl
                .select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(PhysicalFlow flow) {
        checkNotNull(flow, "flow cannot be null");
        checkFalse(flow.id().isPresent(), "flow must not have an id");

        PhysicalFlowRecord record = dsl.newRecord(PHYSICAL_FLOW);
        record.setTargetEntityKind(flow.target().kind().name());
        record.setTargetEntityId(flow.target().id());

        record.setFrequency(flow.frequency().name());
        record.setTransport(flow.transport().name());
        record.setBasisOffset(flow.basisOffset());

        record.setSpecificationId(flow.specificationId());

        record.setDescription(flow.description());
        record.setProvenance("waltz");

        record.store();
        return record.getId();
    }


    private Select<Record> findByProducerEntityReferenceQuery(EntityReference ref) {

        Condition isSource = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(ref.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(ref.kind().name()));


        return dsl
                .select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .where(dsl.renderInlined(isSource));
    }


    private Select<Record> findByConsumerEntityReferenceQuery(EntityReference ref) {

        Condition isTarget = PHYSICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name())
                .and(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id()));

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .select(targetEntityNameField)
                .from(PHYSICAL_FLOW)
                .where(dsl.renderInlined(isTarget));
    }

}
