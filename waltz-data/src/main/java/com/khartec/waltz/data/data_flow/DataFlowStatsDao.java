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

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowMeasures;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.DataFlow;
import com.khartec.waltz.schema.tables.DataFlowDecorator;
import com.khartec.waltz.schema.tables.DataType;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;


@Repository
public class DataFlowStatsDao {

    private final DSLContext dsl;

    private static final DataFlow df = DATA_FLOW.as("df");
    private static final com.khartec.waltz.schema.tables.DataType dt = DataType.DATA_TYPE.as("dt");
    private static final com.khartec.waltz.schema.tables.DataFlowDecorator dfd = DataFlowDecorator.DATA_FLOW_DECORATOR.as("dfd");


    private static final Condition BOTH_APPS =
            df.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(df.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));


    @Autowired
    public DataFlowStatsDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public DataFlowMeasures countDistinctAppInvolvement(Select<Record1<Long>> appIdSelector) {

        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Select<Record1<Integer>> inAppCounter = FunctionUtilities.time("DFSD.inAppCounter", ()
                -> countDistinctApps(
                    appIdSelector,
                    df.TARGET_ENTITY_ID,
                    df.SOURCE_ENTITY_ID
        ));

        Select<Record1<Integer>> outAppCounter = FunctionUtilities.time("DFSD.outAppCounter", ()
                -> countDistinctApps(
                    appIdSelector,
                    df.SOURCE_ENTITY_ID,
                    df.TARGET_ENTITY_ID
        ));


        Select<Record1<Integer>> intraAppCounter = FunctionUtilities.time("DFSD.intraAppCounter", ()
                -> dsl
                    .select(DSL.count())
                    .from(APPLICATION)
                    .where(dsl.renderInlined(APPLICATION.ID.in(appIdSelector))));

        Select<Record1<Integer>> query = inAppCounter
                .unionAll(outAppCounter)
                .unionAll(intraAppCounter);

        List<Integer> results = FunctionUtilities.time("DFSD.executeUnionAppCounters", ()
                -> query.fetch(0, Integer.class));

        return ImmutableDataFlowMeasures
                .builder()
                .inbound(results.get(0))
                .outbound(results.get(1))
                .intra(results.get(2))
                .build();
    }


    public List<Tally<String>> tallyDataTypes(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Condition condition = df.TARGET_ENTITY_ID.in(appIdSelector)
                .or(df.SOURCE_ENTITY_ID.in(appIdSelector))
                .and(BOTH_APPS);


         // dataType.CODE should no longer be used as a 'pk' (use .ID instead)
         // TODO: fix this, probably simple as client impact hidden behind toDisplayName etc..

        return dsl.select(dt.CODE, DSL.count())
                .from(df)
                .innerJoin(dfd)
                .on(df.ID.eq(dfd.DATA_FLOW_ID))
                .innerJoin(dt)
                .on(dt.ID.eq(dfd.DECORATOR_ENTITY_ID)
                        .and(dfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .where(dsl.renderInlined(condition))
                .groupBy(dt.CODE)
                .fetch(TO_STRING_TALLY);
    }


    public DataFlowMeasures countDistinctFlowInvolvement(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");


        SelectJoinStep<Record2<Long, Long>> flows = dsl
                .selectDistinct(df.SOURCE_ENTITY_ID, df.TARGET_ENTITY_ID)
                .from(df);

        Condition inboundCondition = DSL
                .field("SOURCE_ENTITY_ID").notIn(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").in(appIdSelector));

        Condition outboundCondition = DSL
                .field("SOURCE_ENTITY_ID").in(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").notIn(appIdSelector));

        Condition intraCondition = DSL
                .field("SOURCE_ENTITY_ID").in(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").in(appIdSelector));

        Select<Record1<Integer>> outCounter = dsl
                .selectCount()
                .from(flows)
                .where(dsl.renderInlined(outboundCondition));

        Select<Record1<Integer>> intraCounter = dsl
                .selectCount()
                .from(flows)
                .where(dsl.renderInlined(intraCondition));

        Select<Record1<Integer>> inCounter = dsl
                .selectCount()
                .from(flows)
                .where(dsl.renderInlined(inboundCondition));

        Select<Record1<Integer>> query = inCounter
                .unionAll(outCounter)
                .unionAll(intraCounter);


        List<Integer> results = query.fetch(0, Integer.class);


        return ImmutableDataFlowMeasures.builder()
                .inbound(results.get(0))
                .outbound(results.get(1))
                .intra(results.get(2))
                .build();
    }


    // -- App Counts

    private SelectConditionStep<Record1<Integer>> countDistinctApps(Select<Record1<Long>> appIdSelector,
                                                                            Field<Long> feederField,
                                                                            Field<Long> fieldToCount) {
        Condition condition = fieldToCount
                .notIn(appIdSelector)
                .and(feederField.in(appIdSelector))
                .and(BOTH_APPS);

        return dsl.select(DSL.countDistinct(fieldToCount))
                .from(df)
                .where(dsl.renderInlined(condition));

    }

}
