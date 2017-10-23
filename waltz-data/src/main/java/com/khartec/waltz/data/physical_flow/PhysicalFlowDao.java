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

import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Repository
public class PhysicalFlowDao {


    public static final RecordMapper<Record, PhysicalFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowRecord record = r.into(PHYSICAL_FLOW);
        return ImmutablePhysicalFlow.builder()
                .id(record.getId())
                .provenance(record.getProvenance())
                .specificationId(record.getSpecificationId())
                .basisOffset(record.getBasisOffset())
                .frequency(FrequencyKind.valueOf(record.getFrequency()))
                .criticality(Criticality.valueOf(record.getCriticality()))
                .description(record.getDescription())
                .logicalFlowId(record.getLogicalFlowId())
                .transport(TransportKind.valueOf(record.getTransport()))
                .specificationDefinitionId(Optional.ofNullable(record.getSpecificationDefinitionId()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastAttestedBy(Optional.ofNullable(record.getLastAttestedBy()))
                .lastAttestedAt(Optional.ofNullable(record.getLastAttestedAt()).map(ts -> ts.toLocalDateTime()))
                .isRemoved(record.getIsRemoved())
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


    public List<PhysicalFlow> findByProducerAndConsumer(EntityReference producer, EntityReference consumer) {
        return findByProducerAndConsumerEntityReferenceQuery(producer, consumer)
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


    public Collection<PhysicalFlow> findByLogicalFlowId(long logicalFlowId) {
        return findByCondition(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId));
    }


    public List<PhysicalFlow> findBySelector(Select<Record1<Long>> selector) {
        return findByCondition(PHYSICAL_FLOW.ID.in(selector));
    }


    public List<PhysicalFlow> findByAttributesAndSpecification(PhysicalFlow flow) {

        Condition sameFlow = PHYSICAL_FLOW.SPECIFICATION_ID.eq(flow.specificationId())
                .and(PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset()))
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().name()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().name()))
                .and(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(flow.logicalFlowId()));

        return findByCondition(sameFlow);
    }


    public int delete(long flowId) {
        return dsl.delete(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }


    private List<PhysicalFlow> findByCondition(Condition condition) {
        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(PhysicalFlow flow) {
        checkNotNull(flow, "flow cannot be null");
        checkFalse(flow.id().isPresent(), "flow must not have an id");

        PhysicalFlowRecord record = dsl.newRecord(PHYSICAL_FLOW);
        record.setLogicalFlowId(flow.logicalFlowId());

        record.setFrequency(flow.frequency().name());
        record.setTransport(flow.transport().name());
        record.setBasisOffset(flow.basisOffset());
        record.setCriticality(flow.criticality().name());

        record.setSpecificationId(flow.specificationId());

        record.setDescription(flow.description());
        record.setLastUpdatedBy(flow.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(flow.lastUpdatedAt()));
        record.setLastAttestedBy(flow.lastAttestedBy().orElse(null));
        record.setLastAttestedAt(flow.lastAttestedAt().map(ldt -> Timestamp.valueOf(ldt)).orElse(null));
        record.setIsRemoved(flow.isRemoved());
        record.setProvenance("waltz");

        record.store();
        return record.getId();
    }


    public int updateSpecDefinition(String userName, long flowId, long newSpecDefinitionId) {
        checkNotNull(userName, "userName cannot be null");

        return dsl.update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID, newSpecDefinitionId)
                .set(PHYSICAL_FLOW.LAST_UPDATED_BY, userName)
                .set(PHYSICAL_FLOW.LAST_UPDATED_AT, nowUtcTimestamp())
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .and(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID.isNull()
                        .or(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID.ne(newSpecDefinitionId)))
                .execute();
    }


    private Select<Record> findByProducerEntityReferenceQuery(EntityReference producer) {

        Condition isOwner = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(producer.id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(producer.kind().name()));

        Condition isSender = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(producer.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(producer.kind().name()))
                .and(NOT_REMOVED);

        Condition isProducer = isOwner.or(isSender);

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(isProducer));
    }


    private Select<Record> findByConsumerEntityReferenceQuery(EntityReference consumer) {

        Condition matchesLogicalFlow = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(consumer.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(consumer.kind().name()))
                .and(NOT_REMOVED);

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(matchesLogicalFlow));
    }


    private Select<Record> findByProducerAndConsumerEntityReferenceQuery(EntityReference producer, EntityReference consumer) {

        Condition matchesLogicalFlow = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(producer.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(consumer.kind().name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(consumer.id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(consumer.kind().name()))
                .and(LOGICAL_FLOW.IS_REMOVED.isFalse());

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(matchesLogicalFlow));
    }

}
