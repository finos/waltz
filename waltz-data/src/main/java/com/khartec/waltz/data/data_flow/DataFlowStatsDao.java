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
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.schema.tables.DataFlow;
import com.khartec.waltz.schema.tables.DataTypeUsage;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;


@Repository
public class DataFlowStatsDao {

    private final DSLContext dsl;

    private final DataFlow df = DATA_FLOW.as("df");
    private final com.khartec.waltz.schema.tables.DataType dt = DATA_TYPE.as("dt");
    private final DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");

    private final Condition bothApps =
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


    public DataFlowMeasures countDistinctAppInvolvementByDataType(Select<Record1<Long>> dataTypeIdSelector) {

        checkNotNull(dataTypeIdSelector, "dataTypeIdSelector cannot be null");

        Select<Record1<Integer>> inAppCounter = FunctionUtilities.time("DFSD.inAppCounter", ()
                -> countDistinctAppsForDataType(
                dataTypeIdSelector,
                df.SOURCE_ENTITY_ID
        ));

        Select<Record1<Integer>> outAppCounter = FunctionUtilities.time("DFSD.outAppCounter", ()
                -> countDistinctAppsForDataType(
                dataTypeIdSelector,
                df.TARGET_ENTITY_ID
        ));


        Select<Record1<Integer>> intraAppCounter = FunctionUtilities.time("DFSD.intraAppCounter", () -> {

            Condition condition = dtu.DATA_TYPE_CODE
                    .in(DSL.select(dt.CODE).from(dt).where(dt.ID.in(dataTypeIdSelector))
                            .and(dtu.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

            return dsl
                    .select(DSL.countDistinct(dtu.ENTITY_ID))
                    .from(dtu)
                    .where(dsl.renderInlined(
                            condition));
        });

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


    public List<StringTally> tallyDataTypes(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        return tallyDataTypes(appIdSelector, DSL.select(dt.ID).from(dt));
    }


    public List<StringTally> tallyDataTypes(Select<Record1<Long>> appIdSelector, Select<Record1<Long>> dataTypeIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(dataTypeIdSelector, "dataTypeIdSelector cannot be null");

        Condition condition = df.TARGET_ENTITY_ID.in(appIdSelector)
                .or(df.SOURCE_ENTITY_ID.in(appIdSelector))
                .and(bothApps)
                .and(df.DATA_TYPE.in(
                        DSL.select(dt.CODE).from(dt).where(dt.ID.in(dataTypeIdSelector))
                ));

        return calculateStringTallies(
                dsl,
                df,
                df.DATA_TYPE,
                condition);
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


    public DataFlowMeasures countDistinctFlowInvolvementForDataTyoe(Select<Record1<Long>> dataTypeIdSelector) {
        checkNotNull(dataTypeIdSelector, "dataTypeIdSelector cannot be null");


        SelectConditionStep<Record2<Long, Long>> flows = dsl
                .selectDistinct(df.SOURCE_ENTITY_ID, df.TARGET_ENTITY_ID)
                .from(df)
                .where(df.DATA_TYPE
                        .in(DSL.select(dt.CODE).from(dt).where(dt.ID.in(dataTypeIdSelector))));



        Select<Record1<Integer>> flowCounter = dsl
                .selectCount()
                .from(flows);

        Select<Record1<Integer>> query = flowCounter;


        List<Integer> results = query.fetch(0, Integer.class);


        return ImmutableDataFlowMeasures.builder()
                .inbound(results.get(0))
                .outbound(results.get(0))
                .intra(results.get(0))
                .build();
    }


    // -- App Counts

    private SelectConditionStep<Record1<Integer>> countDistinctApps(Select<Record1<Long>> appIdSelector,
                                                                    Field<Long> feederField,
                                                                    Field<Long> fieldToCount) {
        Condition condition = fieldToCount
                .notIn(appIdSelector)
                .and(feederField.in(appIdSelector))
                .and(bothApps);

        return dsl.select(DSL.countDistinct(fieldToCount))
                .from(df)
                .where(dsl.renderInlined(condition));

    }


    private SelectConditionStep<Record1<Integer>> countDistinctAppsForDataType(Select<Record1<Long>> dataTypeIdSelector,
                                                                               Field<Long> fieldToCount) {
        Condition condition = df.DATA_TYPE
                .in(DSL.select(dt.CODE).from(dt).where(dt.ID.in(dataTypeIdSelector)))
                .and(bothApps);

        return dsl.select(DSL.countDistinct(fieldToCount))
                .from(df)
                .where(dsl.renderInlined(condition));

    }

}
