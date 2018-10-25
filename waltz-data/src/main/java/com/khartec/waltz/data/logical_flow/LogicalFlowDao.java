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

package com.khartec.waltz.data.logical_flow;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.schema.tables.records.LogicalFlowRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;


@Repository
public class LogicalFlowDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDao.class);

    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    public static final RecordMapper<Record, LogicalFlow> TO_DOMAIN_MAPPER = r -> {
        LogicalFlowRecord record = r.into(LogicalFlowRecord.class);

        return ImmutableLogicalFlow.builder()
                .id(record.getId())
                .source(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getSourceEntityKind()))
                        .id(record.getSourceEntityId())
                        .name(ofNullable(r.getValue(SOURCE_NAME_FIELD)))
                        .build())
                .target(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getTargetEntityKind()))
                        .id(record.getTargetEntityId())
                        .name(ofNullable(r.getValue(TARGET_NAME_FIELD)))
                        .build())
                .entityLifecycleStatus(readEnum(record.getEntityLifecycleStatus(), EntityLifecycleStatus.class, s -> EntityLifecycleStatus.ACTIVE))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastAttestedBy(Optional.ofNullable(record.getLastAttestedBy()))
                .lastAttestedAt(Optional.ofNullable(record.getLastAttestedAt()).map(ts -> ts.toLocalDateTime()))
                .provenance(record.getProvenance())
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
        record.setLastAttestedAt(flow.lastAttestedAt().map(ldt -> Timestamp.valueOf(ldt)).orElse(null));
        record.setProvenance(flow.provenance());
        record.setEntityLifecycleStatus(flow.entityLifecycleStatus().name());
        return record;
    };


    public static final Condition NOT_REMOVED = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());


    private final DSLContext dsl;


    @Autowired
    public LogicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return baseQuery()
                .where(isSourceOrTargetCondition(ref))
                .and(NOT_REMOVED)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public LogicalFlow getBySourceAndTarget(EntityReference source, EntityReference target) {
        return baseQuery()
                .where(isSourceCondition(source))
                .and(isTargetCondition(target))
                .and(NOT_REMOVED)
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
                        .and(NOT_REMOVED))
                .reduce((a, b) -> a.or(b))
                .get();

        List<LogicalFlow> fetch = baseQuery()
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);

        return fetch;
    }


    public Collection<LogicalFlow> findUpstreamFlowsForEntityReferences(List<EntityReference> references) {

        Map<EntityKind, Collection<EntityReference>> refsByKind = groupBy(ref -> ref.kind(), references);

        Condition anyTargetMatches = refsByKind
                .entrySet()
                .stream()
                .map(entry -> LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(entry.getKey().name())
                        .and(LOGICAL_FLOW.TARGET_ENTITY_ID.in(map(entry.getValue(), ref -> ref.id()))))
                .collect(Collectors.reducing(DSL.falseCondition(), (acc, c) -> acc.or(c)));

        return baseQuery()
                .where(anyTargetMatches)
                .and(LogicalFlowDao.NOT_REMOVED)
                .fetch()
                .map(TO_DOMAIN_MAPPER);
    }


    public int removeFlow(Long flowId, String user) {
        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
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


    public List<LogicalFlow> addFlows(List<LogicalFlow> flows, String user) {
        Condition condition = flows
                .stream()
                .map(t -> isSourceCondition(t.source())
                        .and(isTargetCondition(t.target()))
                        .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(REMOVED.name())))
                .reduce((a, b) -> a.or(b))
                .get();

        List<LogicalFlow> removedFlows = baseQuery()
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);

        if(removedFlows.size() > 0) {
            restoreFlows(removedFlows, user);
        }

        Map<Tuple2<EntityReference, EntityReference>, LogicalFlow> existing = removedFlows
                .stream()
                .collect(Collectors.toMap(f -> Tuple.tuple(f.source(), f.target()), f -> f));


        List<LogicalFlow> addedFlows = flows
                .stream()
                .filter(f -> !existing.containsKey(Tuple.tuple(f.source(), f.target())))
                .map(f -> {
                    LogicalFlowRecord record = TO_RECORD_MAPPER.apply(f, dsl);
                    record.store();
                    return ImmutableLogicalFlow
                            .copyOf(f)
                            .withId(record.getId());
                })
                .collect(toList());


        addedFlows.addAll(removedFlows);
        return addedFlows;
    }


    /**
     * Attempt to restore a flow.  The id is ignored and only source and target
     * are used. Return's true if the flow has been successfully restored or
     * false if no matching (removed) flow was found.
     * @param flow
     * @return
     */
    private boolean restoreFlow(LogicalFlow flow, String username) {
        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(flow.source().id()))
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(flow.source().kind().name()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(flow.target().id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(flow.target().kind().name()))
                .execute() == 1;
    }


    public boolean restoreFlow(long logicalFlowId, String username) {
        return dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(LOGICAL_FLOW.ID.eq(logicalFlowId))
                .execute() == 1;
    }


    private int restoreFlows(List<LogicalFlow> flows, String username) {
        if(flows.isEmpty()) {
            return 0;
        }

        Condition condition = flows
                .stream()
                .map(t -> isSourceCondition(t.source())
                        .and(isTargetCondition(t.target()))
                        .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(REMOVED.name())))
                .reduce((a, b) -> a.or(b))
                .get();

        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, ACTIVE.name())
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(condition)
                .execute();
    }


    public LogicalFlow getByFlowId(long dataFlowId) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.eq(dataFlowId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<LogicalFlow> findAllActive() {
        return baseQuery()
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public List<LogicalFlow> findByFlowIds(Collection<Long> dataFlowIds) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.in(dataFlowIds))
                .and(NOT_REMOVED)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<LogicalFlow> findBySelector(Select<Record1<Long>> flowIdSelector) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.in(flowIdSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Integer cleanupOrphans() {
        Select<Record1<Long>> appIds = DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(IS_ACTIVE);

        Condition sourceAppNotFound = LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.notIn(appIds));

        Condition targetAppNotFound = LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.notIn(appIds));

        Condition notRemoved = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());

        Condition requiringCleanup = notRemoved
                .and(sourceAppNotFound.or(targetAppNotFound));

        List<Long> flowIds = dsl.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(requiringCleanup)
                .fetch(LOGICAL_FLOW.ID);

        LOG.info("Logical flow cleanupOrphans. The following flows will be marked as removed as one or both endpoints no longer exist: {}", flowIds);

        return dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS, REMOVED.name())
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
                .from(LOGICAL_FLOW);
    }

}
