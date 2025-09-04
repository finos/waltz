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

package org.finos.waltz.data.physical_flow;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.data.enum_value.EnumValueDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.enum_value.EnumValueKind;
import org.finos.waltz.model.physical_flow.CriticalityValue;
import org.finos.waltz.model.physical_flow.FrequencyKindValue;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlow;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowInfo;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlowInfo;
import org.finos.waltz.model.physical_flow.PhysicalFlowParsed;
import org.finos.waltz.model.physical_flow.TransportKindValue;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.PhysicalSpecDataType;
import org.finos.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkFalse;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.DATA_TYPE;
import static org.finos.waltz.schema.Tables.EXTERNAL_IDENTIFIER;
import static org.finos.waltz.schema.Tables.PHYSICAL_SPEC_DATA_TYPE;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.impl.DSL.select;


@Repository
public class PhysicalFlowDao {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowDao.class);

    public static final RecordMapper<Record, PhysicalFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowRecord record = r.into(PHYSICAL_FLOW);
        return ImmutablePhysicalFlow.builder()
                .id(record.getId())
                .name(record.getName())
                .provenance(record.getProvenance())
                .specificationId(record.getSpecificationId())
                .basisOffset(record.getBasisOffset())
                .frequency(FrequencyKindValue.of(record.getFrequency()))
                .criticality(CriticalityValue.of(record.getCriticality()))
                .description(record.getDescription())
                .logicalFlowId(record.getLogicalFlowId())
                .transport(TransportKindValue.of(record.getTransport()))
                .specificationDefinitionId(Optional.ofNullable(record.getSpecificationDefinitionId()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastAttestedBy(Optional.ofNullable(record.getLastAttestedBy()))
                .lastAttestedAt(Optional.ofNullable(record.getLastAttestedAt()).map(Timestamp::toLocalDateTime))
                .isRemoved(record.getIsRemoved())
                .externalId(record.getExternalId())
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getEntityLifecycleStatus()))
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .isReadOnly(record.getIsReadonly())
                .build();
    };

    public static final Condition PHYSICAL_FLOW_NOT_REMOVED = PHYSICAL_FLOW.IS_REMOVED.isFalse()
            .and(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));


    private final DSLContext dsl;


    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

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
                .union(producedFlows)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findByProducer(EntityReference ref) {
        return findByProducerEntityReferenceQuery(ref)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findByExternalId(String externalId) {
        return dsl
                .selectDistinct(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .leftJoin(EXTERNAL_IDENTIFIER)
                .on(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(PHYSICAL_FLOW.ID)
                        .and(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())))
                .where(PHYSICAL_FLOW.EXTERNAL_ID.eq(externalId).or(EXTERNAL_IDENTIFIER.EXTERNAL_ID.eq(externalId)))
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


    public PhysicalFlow getByIdAndIsRemoved(long id, boolean isRemoved) {
        return findByCondition(PHYSICAL_FLOW.ID.eq(id).and(PHYSICAL_FLOW.IS_REMOVED.eq(isRemoved)))
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


    public List<PhysicalFlow> findByAttributesAndSpecification(PhysicalFlow flow) {

        Condition nameCondition = flow.name() == null
                ? PHYSICAL_FLOW.NAME.isNull()
                :PHYSICAL_FLOW.NAME.eq(flow.name());

        Condition sameFlow = PHYSICAL_FLOW.SPECIFICATION_ID.eq(flow.specificationId())
                .and(nameCondition)
                .and(PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset()))
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().value()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().value()))
                .and(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(flow.logicalFlowId()))
                .and(PHYSICAL_FLOW.IS_REMOVED.isFalse());

        return findByCondition(sameFlow);
    }


    public PhysicalFlow getByParsedFlow(PhysicalFlowParsed flow) {
        Condition attributesMatch = PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset())
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().value()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().value()))
                .and(PHYSICAL_FLOW.CRITICALITY.eq(flow.criticality().value()))
                .and(PHYSICAL_FLOW_NOT_REMOVED);

        Condition logicalFlowMatch = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(flow.source().id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(flow.source().kind().name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(flow.target().id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(flow.target().kind().name()))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Condition specMatch = PHYSICAL_SPECIFICATION.OWNING_ENTITY_ID.eq(flow.owner().id())
                .and(PHYSICAL_SPECIFICATION.OWNING_ENTITY_KIND.eq(flow.owner().kind().name()))
                .and(PHYSICAL_SPECIFICATION.FORMAT.eq(flow.format().value()))
                .and(PHYSICAL_SPECIFICATION.NAME.eq(flow.name()))
                .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse());

        Condition specDataTypeMatch = PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(flow.dataType().id());


        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .join(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .join(PHYSICAL_SPECIFICATION).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .join(PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE).on(PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .where(logicalFlowMatch)
                .and(specMatch)
                .and(attributesMatch)
                .and(specDataTypeMatch)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    /**
     * Returns the flow in the database that matches the parameter based on all attributes except possibly id
     *
     * @param flow the physical flow to match against
     * @return matching flow or null
     */
    public PhysicalFlow matchPhysicalFlow(PhysicalFlow flow) {

        Condition idCondition = flow.id().isPresent()
                ? PHYSICAL_FLOW.ID.eq(flow.id().get())
                : DSL.trueCondition();

        return dsl
                .select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(flow.logicalFlowId()))
                .and(PHYSICAL_FLOW.SPECIFICATION_ID.eq(flow.specificationId()))
                .and(PHYSICAL_FLOW.BASIS_OFFSET.eq(flow.basisOffset()))
                .and(PHYSICAL_FLOW.FREQUENCY.eq(flow.frequency().value()))
                .and(PHYSICAL_FLOW.TRANSPORT.eq(flow.transport().value()))
                .and(PHYSICAL_FLOW.CRITICALITY.eq(flow.criticality().value()))
                .and(idCondition)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    /**
     * Soft deletes the physical flow in the database that matches the parameter flowId
     *
     * @param flowId the physical flow id to match against
     * @return soft deleted flow count or 0
     */
    public int delete(long flowId) {
        return dsl.update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.IS_REMOVED, true)
                .set(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .and(PHYSICAL_FLOW.IS_READONLY.isFalse())
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

        record.setName(flow.name());
        record.setFrequency(flow.frequency().value());
        record.setTransport(flow.transport().value());
        record.setBasisOffset(flow.basisOffset());
        record.setCriticality(flow.criticality().value());

        record.setSpecificationId(flow.specificationId());

        record.setDescription(flow.description());
        record.setLastUpdatedBy(flow.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(flow.lastUpdatedAt()));
        record.setLastAttestedBy(flow.lastAttestedBy().orElse(null));
        record.setLastAttestedAt(flow.lastAttestedAt().map(Timestamp::valueOf).orElse(null));
        record.setIsRemoved(flow.isRemoved());
        record.setProvenance("waltz");

        flow.externalId().ifPresent(record::setExternalId);

        record.setCreatedAt(flow.created().map(UserTimestamp::atTimestamp).orElse(Timestamp.valueOf(flow.lastUpdatedAt())));
        record.setCreatedBy(flow.created().map(UserTimestamp::by).orElse(flow.lastUpdatedBy()));

        record.store();
        return record.getId();
    }


    public int updateSpecDefinition(String userName, long flowId, long newSpecDefinitionId) {
        checkNotNull(userName, "userName cannot be null");

        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID, newSpecDefinitionId)
                .set(PHYSICAL_FLOW.LAST_UPDATED_BY, userName)
                .set(PHYSICAL_FLOW.LAST_UPDATED_AT, nowUtcTimestamp())
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .and(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID.isNull()
                        .or(PHYSICAL_FLOW.SPECIFICATION_DEFINITION_ID.ne(newSpecDefinitionId)))
                .execute();
    }


    public int cleanupOrphans() {
        Select<Record1<Long>> allLogicalFlowIds = select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Select<Record1<Long>> allPhysicalSpecs = select(PHYSICAL_SPECIFICATION.ID)
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
        Condition isSender = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(producer.id())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(producer.kind().name()))
                .and(LOGICAL_NOT_REMOVED);

        return dsl
                .selectDistinct(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID))
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(isSender))
                .and(PHYSICAL_FLOW_NOT_REMOVED);
    }


    private Select<Record> findByConsumerEntityReferenceQuery(EntityReference consumer) {
        Condition matchesLogicalFlow = LOGICAL_FLOW.TARGET_ENTITY_ID.eq(consumer.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(consumer.kind().name()))
                .and(LOGICAL_NOT_REMOVED);

        return dsl
                .selectDistinct(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(matchesLogicalFlow))
                .and(PHYSICAL_FLOW_NOT_REMOVED);
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


    public int updateCriticality(long flowId, CriticalityValue criticality) {
        return updateEnum(flowId, PHYSICAL_FLOW.CRITICALITY, criticality.value());
    }


    public int updateFrequency(long flowId, FrequencyKindValue frequencyKind) {
        return updateEnum(flowId, PHYSICAL_FLOW.FREQUENCY, frequencyKind.value());
    }


    public int updateExternalId(long flowId, String externalId) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.EXTERNAL_ID, externalId)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }


    public int updateTransport(long flowId, String transport) {
        Condition enumValueExists = EnumValueDao.mkExistsCondition(EnumValueKind.TRANSPORT_KIND, transport);
        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.TRANSPORT, transport)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .and(enumValueExists)
                .execute();
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


    public int updateEntityLifecycleStatus(long flowId, EntityLifecycleStatus entityLifecycleStatus) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS, entityLifecycleStatus.name())
                .set(PHYSICAL_FLOW.IS_REMOVED, entityLifecycleStatus == EntityLifecycleStatus.REMOVED)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }


    public boolean hasPhysicalFlows(long logicalFlowId) {
        return dsl.fetchCount(select(PHYSICAL_FLOW.ID)
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId))
                .and(PHYSICAL_FLOW.IS_REMOVED.eq(false))) > 0;
    }


    public Set<PhysicalFlowInfo> findUnderlyingPhysicalFlows(Long logicalFlowId) {

        Map<Long, List<EntityReference>> specIdToDataTypeList = dsl
                .select(PHYSICAL_SPECIFICATION.ID, DATA_TYPE.ID, DATA_TYPE.NAME)
                .from(PHYSICAL_SPECIFICATION)
                .innerJoin(PHYSICAL_FLOW).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_FLOW.SPECIFICATION_ID)
                        .and(PHYSICAL_FLOW.IS_REMOVED.isFalse()
                                .and(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(logicalFlowId))))
                .innerJoin(PHYSICAL_SPEC_DATA_TYPE).on(PHYSICAL_SPECIFICATION.ID.eq(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID))
                .innerJoin(DATA_TYPE).on(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(DATA_TYPE.ID))
                .fetchGroups(PHYSICAL_SPECIFICATION.ID, r -> mkRef(EntityKind.DATA_TYPE, r.get(DATA_TYPE.ID), r.get(DATA_TYPE.NAME)));

        Set<PhysicalFlowInfo> physicalFlowDetails = dsl
                .select(LOGICAL_FLOW.SOURCE_ENTITY_ID,
                        LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                        SOURCE_NAME_FIELD,
                        LOGICAL_FLOW.TARGET_ENTITY_ID,
                        LOGICAL_FLOW.TARGET_ENTITY_KIND,
                        TARGET_NAME_FIELD,
                        LOGICAL_FLOW.ID,
                        PHYSICAL_SPECIFICATION.ID,
                        PHYSICAL_SPECIFICATION.NAME,
                        PHYSICAL_FLOW.EXTERNAL_ID,
                        PHYSICAL_FLOW.DESCRIPTION,
                        PHYSICAL_FLOW.TRANSPORT,
                        PHYSICAL_FLOW.FREQUENCY,
                        PHYSICAL_FLOW.CRITICALITY)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION).on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(LOGICAL_FLOW).on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .leftJoin(PHYSICAL_SPEC_DATA_TYPE).on(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .leftJoin(DATA_TYPE).on(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(DATA_TYPE.ID))
                .where(LOGICAL_FLOW.ID.eq(logicalFlowId)
                        .and(PHYSICAL_FLOW.IS_REMOVED.isFalse().and(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())
                                .and(LOGICAL_FLOW.IS_REMOVED.isFalse().and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                                        .and(PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse())))))
                .fetchSet(r -> ImmutablePhysicalFlowInfo
                        .builder()
                        .source(mkRef(EntityKind.valueOf(r.get(LOGICAL_FLOW.SOURCE_ENTITY_KIND)), r.get(LOGICAL_FLOW.SOURCE_ENTITY_ID), r.get(SOURCE_NAME_FIELD)))
                        .target(mkRef(EntityKind.valueOf(r.get(LOGICAL_FLOW.TARGET_ENTITY_KIND)), r.get(LOGICAL_FLOW.TARGET_ENTITY_ID), r.get(TARGET_NAME_FIELD)))
                        .logicalFlow(mkRef(EntityKind.LOGICAL_DATA_FLOW, r.get(LOGICAL_FLOW.ID)))
                        .specification(mkRef(EntityKind.PHYSICAL_SPECIFICATION, r.get(PHYSICAL_SPECIFICATION.ID), r.get(PHYSICAL_SPECIFICATION.NAME)))
                        .physicalFlowDescription(r.get(PHYSICAL_FLOW.DESCRIPTION))
                        .physicalFlowExternalId(r.get(PHYSICAL_FLOW.EXTERNAL_ID))
                        .frequencyKind(FrequencyKindValue.of(r.get(PHYSICAL_FLOW.FREQUENCY)))
                        .transportKindValue(TransportKindValue.of(r.get(PHYSICAL_FLOW.TRANSPORT)))
                        .criticality(CriticalityValue.of(r.get(PHYSICAL_FLOW.CRITICALITY)))
                        .dataTypes(specIdToDataTypeList.getOrDefault(r.get(PHYSICAL_SPECIFICATION.ID), emptyList()))
                        .build());

        return physicalFlowDetails;
    }
    // --- helpers

    private int updateEnum(long flowId, TableField<PhysicalFlowRecord, String> field, String value) {
        return dsl
                .update(PHYSICAL_FLOW)
                .set(field, value)
                .where(PHYSICAL_FLOW.ID.eq(flowId))
                .execute();
    }

    public List<PhysicalFlow> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return newArrayList();
        }

        Condition likeName = JooqUtilities.mkBasicTermSearch(PHYSICAL_FLOW.NAME, terms);
        Condition likeDesc = JooqUtilities.mkBasicTermSearch(PHYSICAL_FLOW.DESCRIPTION, terms);
        Condition likeExternalIdentifier = JooqUtilities.mkStartsWithTermSearch(PHYSICAL_FLOW.EXTERNAL_ID, terms)
                .or(JooqUtilities.mkStartsWithTermSearch(EXTERNAL_IDENTIFIER.EXTERNAL_ID, terms));

        Condition searchFilter = likeName.or(likeDesc).or(likeExternalIdentifier);

        // Join with PHYSICAL_SPECIFICATION to access owning_entity_kind/id
        SelectQuery<Record> query = dsl
                .selectDistinct(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .leftOuterJoin(EXTERNAL_IDENTIFIER)
                .on(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(EntityKind.PHYSICAL_FLOW.name())
                        .and(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(Tables.PHYSICAL_FLOW.ID)))
                .where(PHYSICAL_FLOW.IS_REMOVED.eq(false))
                .and(searchFilter)
                .orderBy(PHYSICAL_FLOW.NAME)
                .limit(options.limit())
                .getQuery();

                return query.fetch(r -> {
            PhysicalFlow flow = PhysicalFlowDao.TO_DOMAIN_MAPPER.map(r);
            return ImmutablePhysicalFlow
                    .copyOf(flow)
                    .withDescription(flow.description());
        });
    }

}
