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

package com.khartec.waltz.data.logical_flow;

import com.khartec.waltz.data.EntityNameUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.schema.tables.records.LogicalFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.Optional.ofNullable;


@Repository
public class LogicalFlowDao {

    private static final Field<String> SOURCE_NAME_FIELD = EntityNameUtilities.mkEntityNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private static final Field<String> TARGET_NAME_FIELD = EntityNameUtilities.mkEntityNameField(
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
                .isRemoved(record.getIsRemoved())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
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
        record.setProvenance(flow.provenance());
        record.setIsRemoved(flow.isRemoved());
        return record;
    };


    public static final Condition NOT_REMOVED = LOGICAL_FLOW.IS_REMOVED.isFalse();


    private final DSLContext dsl;


    @Autowired
    public LogicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<LogicalFlow> findByEntityReference(EntityReference ref) {
        return baseQuery()
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(ref.id()))
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(ref.id()))
                .and(NOT_REMOVED)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public LogicalFlow findBySourceAndTarget(EntityReference source, EntityReference target) {
        return baseQuery()
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(source.id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(target.id()))
                .and(NOT_REMOVED)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public int removeFlow(Long flowId, String user) {
        return dsl.update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.IS_REMOVED, true)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, user)
                .where(LOGICAL_FLOW.ID.eq(flowId))
                .execute();
    }


    public LogicalFlow addFlow(LogicalFlow flow) {
        if (restoreFlow(flow, flow.lastUpdatedBy())) {
            return findBySourceAndTarget(flow.source(), flow.target());
        } else {
            LogicalFlowRecord record = TO_RECORD_MAPPER.apply(flow, dsl);
            record.store();
            return ImmutableLogicalFlow
                    .copyOf(flow)
                    .withId(record.getId());
        }
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
                .set(LOGICAL_FLOW.IS_REMOVED, false)
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
                .set(LOGICAL_FLOW.IS_REMOVED, false)
                .set(LOGICAL_FLOW.LAST_UPDATED_BY, username)
                .set(LOGICAL_FLOW.LAST_UPDATED_AT, Timestamp.valueOf(nowUtc()))
                .where(LOGICAL_FLOW.ID.eq(logicalFlowId))
                .execute() == 1;
    }


    public LogicalFlow findByFlowId(long dataFlowId) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.eq(dataFlowId))
                .fetchOne(TO_DOMAIN_MAPPER);
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
                .and(NOT_REMOVED)
                .fetch(TO_DOMAIN_MAPPER);
    }


    // -- HELPERS ---

    private SelectJoinStep<Record> baseQuery() {
        return dsl
                .select(LOGICAL_FLOW.fields())
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .from(LOGICAL_FLOW);
    }

}
