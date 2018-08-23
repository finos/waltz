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

package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;


@Repository
public class PhysicalFlowDao {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowDao.class);

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
                .externalId(Optional.ofNullable(record.getExternalId()))
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


    public PhysicalFlow getByParsedFlow(PhysicalFlowParsed flow) {
        Condition attributesMatch = PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset())
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().name()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().name()))
                .and(PHYSICAL_FLOW.CRITICALITY.eq(flow.criticality().name()))
                .and(PHYSICAL_FLOW.IS_REMOVED.isFalse());

        Condition logicalFlowMatch = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(flow.source().id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(flow.source().kind().name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(flow.target().id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(flow.target().kind().name()))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Condition specMatch = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(flow.owner().id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(flow.owner().kind().name()))
                .and(PHYSICAL_SPECIFICATION.FORMAT.eq(flow.format().name()))
                .and(PHYSICAL_SPECIFICATION.NAME.eq(flow.name()))
                .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse());

        Condition specDataTypeMatch = PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(flow.dataType().id());


        PhysicalFlow match = dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .join(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .join(PHYSICAL_SPECIFICATION).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .join(PHYSICAL_SPEC_DATA_TYPE).on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .where(logicalFlowMatch)
                .and(specMatch)
                .and(attributesMatch)
                .and(specDataTypeMatch)
                .fetchOne(TO_DOMAIN_MAPPER);

        return match;
    }


    /**
     * Returns the flow in the database that matches the parameter based on all attributes except possibly id
     * @param flow
     * @return
     */
    public PhysicalFlow getByPhysicalFlow(PhysicalFlow flow) {

        Condition idCondition = flow.id().isPresent()
                ? PHYSICAL_FLOW.ID.eq(flow.id().get())
                : DSL.trueCondition();

        return dsl.selectFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(flow.logicalFlowId()))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.eq(flow.specificationId()))
                .and(PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset()))
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().name()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().name()))
                .and(PHYSICAL_FLOW.CRITICALITY.eq(flow.criticality().name()))
                .and(idCondition)
                .fetchOne(TO_DOMAIN_MAPPER);
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
        record.setExternalId(flow.externalId().orElse(null));

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


    public int cleanupOrphans() {
        Select<Record1<Long>> allLogicalFlowIds = DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Select<Record1<Long>> allPhysicalSpecs = DSL.select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.IS_REMOVED.eq(false));

        Condition missingLogical = PHYSICAL_FLOW.LOGICAL_FLOW_ID.notIn(allLogicalFlowIds);
        Condition missingSpec = PHYSICAL_FLOW.SPECIFICATION_ID.notIn(allPhysicalSpecs);
        Condition notRemoved = PHYSICAL_FLOW.IS_REMOVED.eq(false);

        Condition requiringCleanup = notRemoved.and(missingLogical.or(missingSpec));

        List<Long> ids = dsl.select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(requiringCleanup)
                .fetch(PHYSICAL_FLOW.ID);

        LOG.info("Physical flow cleanupOrphans. The following flows will be marked as removed as one or both endpoints no longer exist: {}", ids);

        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.IS_REMOVED, true)
                .where(requiringCleanup)
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
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(matchesLogicalFlow));
    }

    public int updateCriticality(long flowId, Criticality criticality) {
        return updateEnum(flowId, PHYSICAL_FLOW.CRITICALITY, criticality.name());
    }

    public int updateFrequency(long flowId, FrequencyKind frequencyKind) {
        return updateEnum(flowId, PHYSICAL_FLOW.FREQUENCY, frequencyKind.name());
    }

    public int updateTransport(long flowId, TransportKind transportKind) {
        return updateEnum(flowId, PHYSICAL_FLOW.TRANSPORT, transportKind.name());
    }

    public int updateBasisOffset(long flowId, int basis) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.BASIS_OFFSET, basis)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }

    public int updateDescription(long flowId, String description) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.DESCRIPTION, description)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }

    public boolean hasPhysicalFlows(long logicalFlowId) {
        return dsl.fetchCount(DSL.selectFrom(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId))
                .and(PHYSICAL_FLOW.IS_REMOVED.eq(false))) > 0;
    }

    // ---

    private int updateEnum(long flowId, TableField<PhysicalFlowRecord, String> field, String value) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(field, value)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }
}
