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

import com.khartec.waltz.common.ArrayBuilder;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.DataFlowRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static org.jooq.impl.DSL.count;


@Repository
public class DataFlowDao {

    private final DSLContext dsl;
    private final com.khartec.waltz.schema.tables.Application sourceAppAlias = APPLICATION.as("sourceAppAlias");
    private final com.khartec.waltz.schema.tables.Application targetAppAlias = APPLICATION.as("targetAppAlias");
    private final com.khartec.waltz.schema.tables.DataType dt = DATA_TYPE.as("dt");

    private RecordMapper<Record, DataFlow> dataFlowMapper = r -> {
        DataFlowRecord record = r.into(DataFlowRecord.class);
        return ImmutableDataFlow.builder()
                .dataType(record.getDataType())
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
                .rating(Rating.valueOf(record.getRating()))
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public DataFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return baseQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.eq(ref.id()))
                .or(DATA_FLOW.TARGET_ENTITY_ID.eq(ref.id()))
                .fetch(dataFlowMapper);
    }


    public List<DataFlow> findByApplicationIdSelector(Select<Record1<Long>> appIdSelector) {
        return baseQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector))
                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector))
                .fetch(dataFlowMapper);
    }


    public List<DataFlow> findConsumersBySelector(Select<Record1<Long>> appIdSelector,
                                                  EntityReference source,
                                                  String dataType) {

        return baseQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.eq(source.id()))
                .and(DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector))
                .and(DATA_FLOW.DATA_TYPE.eq(dataType))
                .fetch(dataFlowMapper);
    }


    public List<DataFlow> findByApplicationIds(Collection<Long> appIds) {
        return baseQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.in(appIds))
                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIds))
                .fetch(dataFlowMapper);
    }

    private SelectConditionStep<Record> baseQuery() {

        Field[] fields = new ArrayBuilder<Field>()
                .add(DATA_FLOW.fields())
                .add(sourceAppAlias.NAME)
                .add(targetAppAlias.NAME)
                .build(new Field[0]);


        return dsl.select(fields)
                .from(DATA_FLOW)
                .innerJoin(sourceAppAlias)
                .on(DATA_FLOW.SOURCE_ENTITY_ID.eq(sourceAppAlias.ID))
                .innerJoin(targetAppAlias)
                .on(DATA_FLOW.TARGET_ENTITY_ID.eq(targetAppAlias.ID))
                .where(DATA_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
    }


    public int[] removeFlows(List<DataFlow> flows) {
        return removeFlows(dsl, flows);
    }


    public int[] storeFlows(List<DataFlow> flows) {
        return dsl.transactionResult(configuration -> {
            DSLContext tx = DSL.using(configuration);
            removeFlows(tx, flows);
            return addFlows(tx, flows);
        });
    }


    public List<Tally<String>> tallyByDataType() {
        return dsl.select(DATA_FLOW.DATA_TYPE, count(DATA_FLOW.DATA_TYPE))
                .from(DATA_FLOW)
                .groupBy(DATA_FLOW.DATA_TYPE)
                .fetch(r -> ImmutableStringTally.builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
    }


    public Collection<DataFlow> findByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector) {
        return baseQuery()
                .and(DATA_FLOW.DATA_TYPE
                        .in(DSL.select(dt.CODE).from(dt).where(dt.ID.in(dataTypeIdSelector))))
                .fetch(dataFlowMapper);
    }


    private int[] removeFlows(DSLContext ctx, List<DataFlow> flows) {

        List<DeleteConditionStep<DataFlowRecord>> deletes = flows.stream()
                .map(f -> ctx.deleteFrom(DATA_FLOW)
                        .where(DATA_FLOW.SOURCE_ENTITY_ID.eq(f.source().id()))
                        .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(f.source().kind().name()))
                        .and(DATA_FLOW.TARGET_ENTITY_ID.eq(f.target().id()))
                        .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(f.target().kind().name()))
                        .and(DATA_FLOW.DATA_TYPE.eq(f.dataType())))
                .collect(Collectors.toList());

        return ctx.batch(deletes).execute();
    }


    private int[] addFlows(DSLContext ctx, List<DataFlow> flows) {

        List<Insert> inserts = flows.stream()
                .map(f -> ctx
                        .insertInto(DATA_FLOW)
                        .columns(DATA_FLOW.DATA_TYPE,
                                DATA_FLOW.SOURCE_ENTITY_KIND,
                                DATA_FLOW.SOURCE_ENTITY_ID,
                                DATA_FLOW.TARGET_ENTITY_KIND,
                                DATA_FLOW.TARGET_ENTITY_ID,
                                DATA_FLOW.PROVENANCE,
                                DATA_FLOW.RATING)
                        .values(f.dataType(),
                                f.source().kind().name(),
                                f.source().id(),
                                f.target().kind().name(),
                                f.target().id(),
                                f.provenance(),
                                f.rating().name()))
                .collect(Collectors.toList());

        return ctx.batch(inserts).execute();
    }

}
