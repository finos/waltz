/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.data_flow;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.schema.tables.records.DataFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


@Repository
public class DataFlowDao {

    public static final com.khartec.waltz.schema.tables.Application sourceAppAlias = APPLICATION.as("sourceAppAlias");
    public static final com.khartec.waltz.schema.tables.Application targetAppAlias = APPLICATION.as("targetAppAlias");

    public static final RecordMapper<Record, DataFlow> TO_DOMAIN_MAPPER = r -> {
        DataFlowRecord record = r.into(DataFlowRecord.class);
        return ImmutableDataFlow.builder()
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
                .provenance(record.getProvenance())
                .build();
    };


    public static final BiFunction<DataFlow, DSLContext, DataFlowRecord> TO_RECORD_MAPPER = (flow, dsl) -> {
        DataFlowRecord record = dsl.newRecord(DATA_FLOW);
        record.setProvenance(flow.provenance());
        record.setSourceEntityId(flow.source().id());
        record.setSourceEntityKind(flow.source().kind().name());
        record.setTargetEntityId(flow.target().id());
        record.setTargetEntityKind(flow.target().kind().name());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public DataFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return baseQuery()
                .where(DATA_FLOW.SOURCE_ENTITY_ID.eq(ref.id()))
                .or(DATA_FLOW.TARGET_ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<DataFlow> findByApplicationIdSelector(Select<Record1<Long>> appIdSelector) {
        return baseQuery()
                .where(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector))
                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int removeFlows(List<Long> flowIds) {
        return dsl.deleteFrom(DATA_FLOW)
                .where(DATA_FLOW.ID.in(flowIds))
                .execute();
    }


    public DataFlow addFlow(DataFlow flow) {
        DataFlowRecord record = TO_RECORD_MAPPER.apply(flow, dsl);

        record.store();

        return ImmutableDataFlow
                .copyOf(flow)
                .withId(record.getId());
    }


    public DataFlow findByFlowId(long dataFlowId) {
        return baseQuery()
                .where(DATA_FLOW.ID.eq(dataFlowId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public List<DataFlow> findByFlowIds(Collection<Long> dataFlowIds) {
        return baseQuery()
                .where(DATA_FLOW.ID.in(dataFlowIds))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public Collection<DataFlow> findByDataTypeIdSelector(Select<Record1<Long>> typeIdSelector) {
        Condition condition = DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(typeIdSelector))
                .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return baseQuery()
                .innerJoin(DATA_FLOW_DECORATOR)
                .on(DATA_FLOW_DECORATOR.DATA_FLOW_ID.eq(DATA_FLOW.ID))
                .where(dsl.renderInlined(condition))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public Collection<DataFlow> findByPhysicalDataArticleId(long articleId) {
        return baseQuery()
                .innerJoin(PHYSICAL_FLOW)
                .on(DATA_FLOW.ID.eq(PHYSICAL_FLOW.FLOW_ID))
                .where(PHYSICAL_FLOW.ARTICLE_ID.eq(articleId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<DataFlow> findBySelector(Select<Record1<Long>> selector) {
        return baseQuery()
                .where(DATA_FLOW.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    // -- HELPERS ---

    private SelectOnConditionStep<Record> baseQuery() {
        return dsl
                .select(DATA_FLOW.fields())
                .select(sourceAppAlias.NAME, targetAppAlias.NAME)
                .from(DATA_FLOW)
                .innerJoin(sourceAppAlias)
                .on(DATA_FLOW.SOURCE_ENTITY_ID.eq(sourceAppAlias.ID)
                        .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .innerJoin(targetAppAlias)
                .on(DATA_FLOW.TARGET_ENTITY_ID.eq(targetAppAlias.ID)
                        .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
    }

}
