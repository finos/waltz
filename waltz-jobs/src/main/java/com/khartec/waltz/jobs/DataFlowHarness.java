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

package com.khartec.waltz.jobs;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.data_flow.DataFlowStatsDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlowMeasures;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow.DataFlowService;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


public class DataFlowHarness {

    private static Param<String> empIdParam = DSL.param("employeeId", String.class);

    private static SelectConditionStep<Record1<String>> employeeIds = DSL.selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
            .from(PERSON_HIERARCHY)
            .where(PERSON_HIERARCHY.MANAGER_ID.eq(empIdParam));

    private static final SelectConditionStep<Record1<Long>> selector = DSL
            .selectDistinct(INVOLVEMENT.ENTITY_ID)
            .from(INVOLVEMENT)
            .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
            .and(INVOLVEMENT.EMPLOYEE_ID.eq(empIdParam)
                    .or(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));


    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DataFlowService service = ctx.getBean(DataFlowService.class);
        DataFlowStatsDao dataFlowStatsDao = ctx.getBean(DataFlowStatsDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        ApplicationIdSelectionOptions options = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.PERSON)
                        .id(74747)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();


        SelectJoinStep<Record1<Integer>> invCount = dsl
                .select(DSL.countDistinct(INVOLVEMENT.EMPLOYEE_ID).as("C"))
                .from(INVOLVEMENT);


        SelectJoinStep<Record1<Integer>> appCount = dsl
                .select(DSL.countDistinct(APPLICATION.ID).as("C"))
                .from(APPLICATION);


        SelectOrderByStep<Record1<Integer>> union = invCount
                .unionAll(appCount);

        union.stream().forEach(System.out::println);


        FunctionUtilities.time("appCounts", () -> dataFlowStatsDao.countDistinctFlowInvolvement(DSL.select(APPLICATION.ID).from(APPLICATION)));
//



        DataFlowStatistics stats = FunctionUtilities.time("full service", () -> service.calculateStats(options));

        dsl.renderNamedParams(selector);
        empIdParam.setValue("huSs97bwj");
        FunctionUtilities.time("appCounts", () -> dataFlowStatsDao.countDistinctAppInvolvement(DSL.select(APPLICATION.ID).from(APPLICATION)));
        Select<Record1<Long>> subQ = HarnessUtilities.time("build person sub q", () -> mkForPersonReportees("huSs97bwj"));

            HarnessUtilities.time("build complex q", () -> bigQuery(dsl, mkForPersonReportees("huSs97bwj")));



    }


    private static SelectOrderByStep<Record2<String, Integer>> bigQuery(DSLContext dsl, Select<Record1<Long>> appIdSelector) {
        WithStep flows = dsl.with("flows").as(
                DSL.selectDistinct(DATA_FLOW.SOURCE_ENTITY_ID, DATA_FLOW.TARGET_ENTITY_ID)
                        .from(DATA_FLOW));

        ImmutableDataFlowMeasures.Builder builder = ImmutableDataFlowMeasures.builder();


        Condition inboundCondition = DSL
                .field("SOURCE_ENTITY_ID").notIn(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").in(appIdSelector));

        Condition outboundCondition = DSL
                .field("SOURCE_ENTITY_ID").in(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").notIn(appIdSelector));

        Condition intraCondition = DSL
                .field("SOURCE_ENTITY_ID").in(appIdSelector)
                .and(DSL.field("TARGET_ENTITY_ID").in(appIdSelector));


        // TODO:  query goes at least twice as slowly if you don't toString() the condition...
        SelectOrderByStep<Record2<String, Integer>> query = flows.select(DSL.value("inConnCount").as("name"), DSL.count())
                .from("flows")
                .where(inboundCondition.toString())
                .union(DSL
                        .select(DSL.value("outConnCount").as("name"), DSL.count())
                        .from("flows")
                        .where(outboundCondition.toString()))
                .union(DSL
                        .select(DSL.value("intraConnCount").as("name"), DSL.count())
                        .from("flows")
                        .where(intraCondition.toString()));

        return query;

    }


    private static SelectOrderByStep<Record> buildQ(DSLContext dsl, Select<Record1<Long>> selector) {
        return
                dsl.select(DATA_FLOW.fields())
                .from(DATA_FLOW)
                .where(DATA_FLOW.SOURCE_ENTITY_ID.in(selector))
                .union(dsl.select(DATA_FLOW.fields())
                        .where(DATA_FLOW.TARGET_ENTITY_ID.in(selector)));

    }


    private static void foo() {


    }
    private static Select<Record1<Long>> mkForPersonReportees(String employeeId) {

        SelectConditionStep<Record1<String>> employeeIds = DSL.selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId));

        return DSL
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)
                        .or(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));
    }

}
