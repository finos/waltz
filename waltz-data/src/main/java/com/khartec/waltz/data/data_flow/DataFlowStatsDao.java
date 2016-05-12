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

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.dataflow.DataFlowMeasures;
import com.khartec.waltz.model.dataflow.DataFlowQueryOptions;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowMeasures;
import com.khartec.waltz.model.tally.StringTally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.calculateTallies;
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


    public DataFlowMeasures countDistinctAppInvolvement(DataFlowQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");

        List<Long> appIds = options.applicationIds();

        SelectConditionStep<Record2<String, Integer>> inAppCounter = countDistinctApps(
                appIds,
                DATA_FLOW.TARGET_ENTITY_ID,
                "inAppCount",
                DATA_FLOW.SOURCE_ENTITY_ID
        );

        SelectConditionStep<Record2<String, Integer>> outAppCounter = countDistinctApps(
                appIds,
                DATA_FLOW.SOURCE_ENTITY_ID,
                "outAppCount",
                DATA_FLOW.TARGET_ENTITY_ID
        );

        ImmutableDataFlowMeasures.Builder resultBuilder = ImmutableDataFlowMeasures
                .builder()
                .intra(appIds.size());
                // incomplete on purpose, remaining values provided by query result

        inAppCounter.union(outAppCounter)
                .stream()
                .forEach(r -> {
                    Integer appCount = r.value2();
                    switch(r.value1()) {
                        case "inAppCount":
                            resultBuilder.inbound(appCount);
                            break;
                        case "outAppCount":
                            resultBuilder.outbound(appCount);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown data flow measure type: "+r.value1());
                    }
                });

        return resultBuilder.build();
    }



    public List<StringTally> tallyDataTypes(DataFlowQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");

        List<Long> appIds = options.applicationIds();

        Condition condition = DATA_FLOW.TARGET_ENTITY_ID.in(appIds)
                .or(DATA_FLOW.SOURCE_ENTITY_ID.in(appIds))
                .and(bothApps);

        return calculateTallies(
                dsl,
                DATA_FLOW,
                DATA_FLOW.DATA_TYPE,
                condition);
    }


    public DataFlowMeasures countDistinctFlowInvolvement(DataFlowQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");

        List<Long> appIds = options.applicationIds();

        WithStep flows = dsl.with("flows").as(
                DSL.selectDistinct(DATA_FLOW.SOURCE_ENTITY_ID, DATA_FLOW.TARGET_ENTITY_ID)
                        .from(DATA_FLOW));

        ImmutableDataFlowMeasures.Builder builder = ImmutableDataFlowMeasures.builder();

        flows.select(DSL.value("inConnCount").as("name"),
                DSL.count())
                .from("flows")
                .where(DSL.field("SOURCE_ENTITY_ID").notIn(appIds))
                .and(DSL.field("TARGET_ENTITY_ID").in(appIds))
                .union(DSL.select(DSL.value("outConnCount").as("name"),
                        DSL.count())
                        .from("flows")
                        .where(DSL.field("SOURCE_ENTITY_ID").in(appIds))
                        .and(DSL.field("TARGET_ENTITY_ID").notIn(appIds)))
                .union(DSL.select(DSL.value("intraConnCount").as("name"),
                        DSL.count())
                        .from("flows")
                        .where(DSL.field("SOURCE_ENTITY_ID").in(appIds))
                        .and(DSL.field("TARGET_ENTITY_ID").in(appIds)))
                .stream()
                .forEach(r -> {
                    String name = r.value1();
                    int count = r.value2();
                    switch (name) {
                        case "inConnCount":
                            builder.inbound(count);
                            break;
                        case "outConnCount":
                            builder.outbound(count);
                            break;
                        case "intraConnCount":
                            builder.intra(count);
                            break;
                        default:
                            throw new IllegalArgumentException("Cannot handle measure with name: " + name);
                    }
                });

        return builder.build();
    }


    // -- App Counts


    private SelectConditionStep<Record2<String, Integer>> countDistinctApps(List<Long> ids,
                                                                            Field<Long> feederField,
                                                                            String name,
                                                                            Field<Long> fieldToCount) {
        return dsl.select(DSL.value(name), DSL.countDistinct(fieldToCount))
                .from(DATA_FLOW)
                .where(fieldToCount.notIn(ids))
                .and(feederField.in(ids))
                .and(bothApps);
    }


}
