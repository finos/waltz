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
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.jooq.impl.DSL.inline;


@Repository
public class LogicalFlowDao {

    public static final com.khartec.waltz.schema.tables.Application sourceAppAlias = APPLICATION.as("sourceAppAlias");
    public static final com.khartec.waltz.schema.tables.Application targetAppAlias = APPLICATION.as("targetAppAlias");

    public static final RecordMapper<Record, LogicalFlow> TO_DOMAIN_MAPPER = r -> {
        LogicalFlowRecord record = r.into(LogicalFlowRecord.class);

        return ImmutableLogicalFlow.builder()
                .id(record.getId())
                .source(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getSourceEntityKind()))
                        .id(record.getSourceEntityId())
                        .name(r.getValue(sourceAppAlias.NAME))
                        .build())
                .target(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getTargetEntityKind()))
                        .id(record.getTargetEntityId())
                        .name(r.getValue(targetAppAlias.NAME))
                        .build())
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
        return record;
    };


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
                .fetch(TO_DOMAIN_MAPPER);
    }


    public LogicalFlow findBySourceAndTarget(EntityReference source, EntityReference target) {
        return baseQuery()
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(source.id()))
                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(target.id()))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public int removeFlows(List<Long> flowIds) {
        return dsl.deleteFrom(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.ID.in(flowIds))
                .execute();
    }


    public LogicalFlow addFlow(LogicalFlow flow) {
        LogicalFlowRecord record = TO_RECORD_MAPPER.apply(flow, dsl);

        record.store();

        return ImmutableLogicalFlow
                .copyOf(flow)
                .withId(record.getId());
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
                .fetch(TO_DOMAIN_MAPPER);
    }



    public List<LogicalFlow> findBySelector(Select<Record1<Long>> flowIdSelector) {
        return baseQuery()
                .where(LOGICAL_FLOW.ID.in(flowIdSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    // -- HELPERS ---

    private SelectOnConditionStep<Record> baseQuery() {
        return dsl
                .select(LOGICAL_FLOW.fields())
                .select(sourceAppAlias.NAME, targetAppAlias.NAME)
                .from(LOGICAL_FLOW)
                .innerJoin(sourceAppAlias)
                .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(sourceAppAlias.ID)
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(inline(EntityKind.APPLICATION.name()))))
                .innerJoin(targetAppAlias)
                .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(targetAppAlias.ID)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(inline(EntityKind.APPLICATION.name()))));
    }

}
