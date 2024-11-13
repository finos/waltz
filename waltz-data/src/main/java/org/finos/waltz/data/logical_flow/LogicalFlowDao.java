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

package org.finos.waltz.data.logical_flow;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.tables.records.LogicalFlowRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectJoinStep;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.ListUtilities.filter;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.schema.Tables.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.schema.Tables.USER_ROLE;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class LogicalFlowDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDao.class);

    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));


    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Field<String> SOURCE_EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Field<String> TARGET_EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));


    public static final RecordMapper<Record, LogicalFlow> TO_DOMAIN_MAPPER = r -> {
        LogicalFlowRecord record = r.into(LogicalFlowRecord.class);

        return ImmutableLogicalFlow.builder()
                .id(record.getId())
                .externalId(record.getExternalId())
                .source(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getSourceEntityKind()))
                        .id(record.getSourceEntityId())
                        .name(ofNullable(r.getValue(SOURCE_NAME_FIELD)))
                        .externalId(ofNullable(r.getValue(SOURCE_EXTERNAL_ID_FIELD)))
                        .build())
                .target(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getTargetEntityKind()))
                        .id(record.getTargetEntityId())
                        .name(ofNullable(r.getValue(TARGET_NAME_FIELD)))
                        .externalId(ofNullable(r.getValue(TARGET_EXTERNAL_ID_FIELD)))
                        .build())
                .entityLifecycleStatus(readEnum(record.getEntityLifecycleStatus(), EntityLifecycleStatus.class, s -> EntityLifecycleStatus.ACTIVE))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastAttestedBy(Optional.ofNullable(record.getLastAttestedBy()))
                .lastAttestedAt(Optional.ofNullable(record.getLastAttestedAt()).map(Timestamp::toLocalDateTime))
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .provenance(record.getProvenance())
                .isReadOnly(record.getIsReadonly())
                .isRemoved(record.getIsRemoved())
                .build();
    };


    public static final BiFunction<LogicalFlow, DSLContext, LogicalFlowRecord> TO_RECORD_MAPPER = (flow, dsl) -> {
        LogicalFlowRecord record = dsl.newRecord(LOGICAL_FLOW);
        record.setSourceEntityId(flow.source().id());
        record.setSourceEntityKind(flow.source().kind().name());
        record.setTargetEntityId(flow.target().id());
        record.setTargetEntityKind(flow.target().kind().name());
        record.setLastUpdatedBy(flow.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(flow.lastUpdatedAt()));
        record.setLastAttestedBy(flow.lastAttestedBy().orElse(null));
        record.setLastAttestedAt(flow.lastAttestedAt().map(Timestamp::valueOf).orElse(null));
        record.setProvenance(flow.provenance());
        record.setEntityLifecycleStatus(flow.entityLifecycleStatus().name());
        record.setCreatedAt(flow.created().map(UserTimestamp::atTimestamp).orElse(Timestamp.valueOf(flow.lastUpdatedAt())));
        record.setCreatedBy(flow.created().map(UserTimestamp::by).orElse(flow.lastUpdatedBy()));
        record.setIsReadonly(flow.isReadOnly());
        record.setIsRemoved(flow.isRemoved());
        flow.externalId().ifPresent(record::setExternalId);
        return record;
    };


    public static final Condition LOGICAL_NOT_REMOVED = LOGICAL_FLOW.IS_REMOVED.isFalse()
            .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

    public static final Condition SPEC_NOT_REMOVED = PHYSICAL_SPECIFICATION.IS_REMOVED.isFalse();

    private final DSLContext dsl;


    @Autowired
    public LogicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public LogicalFlow getByFlowExternalId(String externalId) {
        return baseQuery()
                .where(LOGICAL_FLOW.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return baseQuery()
                .where(isSourceOrTargetCondition(ref))
                .and(LOGICAL_NOT_REMOVED)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public LogicalFlow getBySourceAndTarget(EntityReference source, EntityReference target) {
        return baseQuery()
                .where(isSourceCondition(source))
                .and(isTargetCondition(target))
                .and(LOGICAL_NOT_REMOVED)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LogicalFlow> findBySourcesAndTargets(List<Tuple2<EntityReference, EntityReference>> sourceAndTargets) {
        if(sourceAndTargets.isEmpty()) {
            return Collections.emptyList();
        }

        Condition condition = sourceAndTargets
                .stream()
                .map(t -> isSourceCondition(t.v1)
                        .and(isTargetCondition(t.v2))
                        .and(LOGICAL_NOT_REMOVED))
                .reduce(Condition::or)
                .get();

        return baseQuery()
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<LogicalFlow> findUpstreamFlowsForEntityReferences(List<EntityReference> references) {

        Map<EntityKind, Collection<EntityReference>> refsByKind = groupBy(
                EntityReference::kind,
                references);

        Condition anyTargetMatches = refsByKind
                .entrySet()
                .stream()
                .map(entry -> LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(entry.getKey().name())
                        .and(LOGICAL_FLOW.TARGET_ENTITY_ID.in(map(entry.getValue(), EntityReference::id))))
                .collect(Collectors.reducing(DSL.falseCondition(), Condition::or));

        return baseQuery()
                .where(anyTargetMatches)
                .and(LogicalFlowDao.LOGICAL_NOT_REMOVED)
                .fetch()
                .map(TO_DOMAIN_MAPPER);
    }


    public int removeFlow(Long flowId, String user) {
        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
                .set(LOGICAL_FLOW.IS_REMOVED, true)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, user)
                .where(LOGICAL_FLOW.ID.eq(flowId))
                .execute();
    }


    public LogicalFlow addFlow(LogicalFlow flow) {
        if (restoreFlow(flow, flow.lastUpdatedBy())) {
            return getBySourceAndTarget(flow.source(), flow.target());
        } else {
            LogicalFlowRecord record = TO_RECORD_MAPPER.apply(flow, dsl);
            record.store();
            return ImmutableLogicalFlow
                    .copyOf(flow)
                    .withId(record.getId());
        }
    }


    public Set<LogicalFlow> addFlows(Set<LogicalFlow> flows, String user) {

        Condition condition = flows
                .stream()
                .map(t -> isSourceCondition(t.source())
                        .and(isTargetCondition(t.target())))
                .reduce(Condition::or)
                .orElse(DSL.falseCondition());

        List<LogicalFlow> existingFlows = baseQuery()
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);

        List<LogicalFlow> removedFlows = filter(
                f -> f.entityLifecycleStatus().equals(REMOVED) || f.isRemoved(),
                existingFlows);

        if(removedFlows.size() > 0) {
            restoreFlows(removedFlows, user);
        }

        Map<Tuple2<EntityReference, EntityReference>, LogicalFlow> existing = indexBy(
                existingFlows,
                f -> tuple(f.source(), f.target()));

        Set<LogicalFlow> addedFlows = flows
                .stream()
                .filter(f -> !existing.containsKey(tuple(f.source(), f.target())))
                .map(f -> {
                    LogicalFlowRecord record = TO_RECORD_MAPPER.apply(f, dsl);
                    record.store();
                    return ImmutableLogicalFlow
                            .copyOf(f)
                            .withId(record.getId());
                })
                .collect(toSet());


        addedFlows.addAll(removedFlows);

        return addedFlows;
    }


    /**
     * Attempt to restore a flow.  The id is ignored and only source and target
     * are used. Return's true if the flow has been successfully restored or
     * false if no matching (removed) flow was found.
     *
     * @param flow logical flow to restore, based on the source and target
     * @return true if the flow was restored
     */
    private boolean restoreFlow(LogicalFlow flow, String username) {
        UpdateConditionStep<LogicalFlowRecord> upd = dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .set(LOGICAL_FLOW.IS_REMOVED, false)
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(flow.source().id()))
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(flow.source().kind().name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(flow.target().id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(flow.target().kind().name()));
        return upd
                .execute() == 1;
    }


    public boolean restoreFlow(long logicalFlowId, String username) {
        return dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.IS_REMOVED, false)
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(LOGICAL_FLOW.ID.eq(logicalFlowId))
                .execute() == 1;
    }



    public LogicalFlow getByFlowId(long dataFlowId) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.eq(dataFlowId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public long updateReadOnly(long flowId, boolean isReadOnly, String user) {
        return dsl
            .update(LOGICAL_FLOW)
            .set(LOGICAL_FLOW.IS_READONLY, isReadOnly)
            .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, user)
            .where(LOGICAL_FLOW.ID.eq(flowId))
            .execute();
    }


    public List<LogicalFlow> findAllActive() {
        return baseQuery()
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LogicalFlow> findActiveByFlowIds(Collection<Long> dataFlowIds) {
        return findByFlowIdsWithCondition(dataFlowIds, LOGICAL_NOT_REMOVED);
    }


    public List<LogicalFlow> findAllByFlowIds(Collection<Long> dataFlowIds) {
        return findByFlowIdsWithCondition(dataFlowIds, DSL.trueCondition());
    }


    public List<LogicalFlow> findBySelector(Select<Record1<Long>> flowIdSelector) {
        return baseQuery()
                .where(dsl.renderInlined(LOGICAL_FLOW.ID.in(flowIdSelector)))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Integer cleanupOrphans() {
        Select<Record1<Long>> appIds = DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(IS_ACTIVE)
                .and(APPLICATION.IS_REMOVED.isFalse());

        Condition sourceAppNotFound = LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.notIn(appIds));

        Condition targetAppNotFound = LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.notIn(appIds));

        Condition notRemoved = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());

        Condition requiringCleanup = notRemoved
                .and(sourceAppNotFound.or(targetAppNotFound));

        List<Long> flowIds = dsl
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(requiringCleanup)
                .fetch(LOGICAL_FLOW.ID);

        LOG.info("Logical flow cleanupOrphans. The following flows will be marked as removed as one or both endpoints no longer exist: {}", flowIds);

        return dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
                .set(LOGICAL_FLOW.IS_REMOVED, true)
                .where(requiringCleanup)
                .execute();
    }


    public int cleanupSelfReferencingFlows() {

        Condition selfReferencing = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(LOGICAL_FLOW.TARGET_ENTITY_KIND));

        Condition notRemoved = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());

        Condition requiringCleanup = notRemoved
                .and(selfReferencing);

        List<Long> flowIds = dsl.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(requiringCleanup)
                .fetch(LOGICAL_FLOW.ID);

        LOG.info("Logical flow cleanupSelfReferencingFlows. The following flows will be marked as removed as one or both endpoints no longer exist: {}", flowIds);

        return dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
                .set(LOGICAL_FLOW.IS_REMOVED, true)
                .where(requiringCleanup)
                .execute();
    }

    // -- HELPERS ---

    private Condition isSourceOrTargetCondition(EntityReference ref) {
        return isSourceCondition(ref)
                .or(isTargetCondition(ref));
    }

    private Condition isTargetCondition(EntityReference ref) {
        return LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()));
    }

    private Condition isSourceCondition(EntityReference ref) {
        return LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                    .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()));
    }

    private SelectJoinStep<Record> baseQuery() {
        return dsl
                .select(LOGICAL_FLOW.fields())
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .select(SOURCE_EXTERNAL_ID_FIELD, TARGET_EXTERNAL_ID_FIELD)
                .from(LOGICAL_FLOW);
    }


    private List<LogicalFlow> findByFlowIdsWithCondition(Collection<Long> dataFlowIds, Condition condition) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.in(dataFlowIds))
                .and(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    private int restoreFlows(List<LogicalFlow> flows, String username) {
        if(flows.isEmpty()) {
            return 0;
        }

        Condition condition = flows
                .stream()
                .map(t -> isSourceCondition(t.source())
                        .and(isTargetCondition(t.target())))
                .reduce(Condition::or)
                .get();

        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.IS_REMOVED, false)
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(condition)
                .execute();
    }


    public Set<Operation> calculateAmendedFlowOperations(Set<Operation> operationsForFlow,
                                                         String username) {

        //TODO: Need to add 'FLOW_ADMIN' permissions for bulk loaders
        boolean hasOverride = dsl
                .fetchExists(DSL
                        .select(USER_ROLE.ROLE)
                        .from(USER_ROLE)
                        .where(USER_ROLE.ROLE.eq(SystemRole.LOGICAL_DATA_FLOW_EDITOR.name())
                                .and(USER_ROLE.USER_NAME.eq(username))));

        if (hasOverride) {
            return union(operationsForFlow, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        } else {
            return operationsForFlow;
        }
    }

}
