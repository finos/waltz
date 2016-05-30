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
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;


@Repository
public class DataFlowStatsDao {

    private final DSLContext dsl;


    private final Condition bothApps =
            DATA_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

    @Autowired
    public DataFlowStatsDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public DataFlowMeasures countDistinctAppInvolvement(Select<Record1<Long>> appIdSelector) {

        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Select<Record1<Integer>> inAppCounter = FunctionUtilities.time("DFSD.inAppCounter", () -> countDistinctApps(
                appIdSelector,
                DATA_FLOW.TARGET_ENTITY_ID,
                DATA_FLOW.SOURCE_ENTITY_ID
        ));

        Select<Record1<Integer>> outAppCounter = FunctionUtilities.time("DFSD.outAppCounter", () -> countDistinctApps(
                appIdSelector,
                DATA_FLOW.SOURCE_ENTITY_ID,
                DATA_FLOW.TARGET_ENTITY_ID
        ));

        Select<Record1<Integer>> intraAppCounter = dsl
                .select(DSL.count())
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appIdSelector));

        Select<Record1<Integer>> query = inAppCounter
                .unionAll(outAppCounter)
                .unionAll(intraAppCounter);

        List<Integer> results = query.fetch(0, Integer.class);

        return ImmutableDataFlowMeasures
                .builder()
                .inbound(results.get(0))
                .outbound(results.get(1))
                .intra(results.get(2))
                .build();
    }



    public List<StringTally> tallyDataTypes(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Condition condition = DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .or(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector))
                .and(bothApps);

        return calculateStringTallies(
                dsl,
                DATA_FLOW,
                DATA_FLOW.DATA_TYPE,
                condition);
    }


    public DataFlowMeasures countDistinctFlowInvolvement(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");


        SelectJoinStep<Record2<Long, Long>> flows = dsl
                .selectDistinct(DATA_FLOW.SOURCE_ENTITY_ID, DATA_FLOW.TARGET_ENTITY_ID)
                .from(DATA_FLOW);

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
                .select(DSL.count())
                .from(flows)
                .where(outboundCondition);

        Select<Record1<Integer>> intraCounter = dsl
                .select(DSL.count())
                .from(flows)
                .where(intraCondition);

        Select<Record1<Integer>> inCounter = dsl
                .select(DSL.count())
                .from(flows)
                .where(inboundCondition);

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
                .and(bothApps);

        return dsl.select(DSL.countDistinct(fieldToCount))
                .from(DATA_FLOW)
                .where(condition);

    }


}
