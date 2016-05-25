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
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.schema.tables.records.DataFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;



@Repository
public class DataFlowDao {

    private final DSLContext dsl;
    private final com.khartec.waltz.schema.tables.Application sourceAppAlias = APPLICATION.as("sourceAppAlias");
    private final com.khartec.waltz.schema.tables.Application targetAppAlias = APPLICATION.as("targetAppAlias");

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
                .provenance(record.getProvenance())
                .build();
    };


    @Autowired
    public DataFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return baseOrgUnitQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.eq(ref.id()).and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name())))
                .or(DATA_FLOW.TARGET_ENTITY_ID.eq(ref.id()).and(DATA_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name())))
                .fetch(dataFlowMapper);
    }


    public List<DataFlow> findByApplicationIdSelector(Select<Record1<Long>> appIdSelector) {
        return baseOrgUnitQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector).toString())
                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector).toString())
                .fetch(dataFlowMapper);
    }

    public List<DataFlow> findByApplicationIds(Collection<Long> appIds) {
        return baseOrgUnitQuery()
                .and(DATA_FLOW.SOURCE_ENTITY_ID.in(appIds))
                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIds))
                .fetch(dataFlowMapper);
    }

    private SelectConditionStep<Record> baseOrgUnitQuery() {

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

        List<DeleteConditionStep<DataFlowRecord>> deletes = flows.stream()
                .map(f -> dsl.deleteFrom(DATA_FLOW)
                        .where(DATA_FLOW.SOURCE_ENTITY_ID.eq(f.source().id()))
                        .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(f.source().kind().name()))
                        .and(DATA_FLOW.TARGET_ENTITY_ID.eq(f.target().id()))
                        .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(f.target().kind().name()))
                        .and(DATA_FLOW.DATA_TYPE.eq(f.dataType())))
                .collect(Collectors.toList());

        return dsl.batch(deletes).execute();
    }


    public int[] addFlows(List<DataFlow> flows) {
        List<Insert> inserts = flows.stream()
                .map(f -> dsl
                        .insertInto(DATA_FLOW)
                        .columns(DATA_FLOW.DATA_TYPE,
                                DATA_FLOW.SOURCE_ENTITY_KIND,
                                DATA_FLOW.SOURCE_ENTITY_ID,
                                DATA_FLOW.TARGET_ENTITY_KIND,
                                DATA_FLOW.TARGET_ENTITY_ID,
                                DATA_FLOW.PROVENANCE)
                        .values(f.dataType(),
                                f.source().kind().name(),
                                f.source().id(),
                                f.target().kind().name(),
                                f.target().id(),
                                f.provenance()))
                .collect(Collectors.toList());

        return dsl.batch(inserts).execute();
    }


}
