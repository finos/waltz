/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.jobs.generators.stress;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import com.khartec.waltz.schema.tables.records.LogicalFlowRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.jobs.WaltzUtilities.*;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.tuple.Tuple.tuple;


/**
 * This generator app creates lots of flows for a given application id.
 * The application is specified via the `appId` variable and the number
 * of flows to generate is specified via the `HOW_MANY_FLOWS` constant.
 *
 * Generated flows alternate between inbound and outbound flows and
 * connect to any active application.
 *
 * Flows are randomly assigned between some data types, the number of
 * data types to assign is bounded by the `MAX_DATA_TYPES_PER_FLOW`
 * constant.
 */
public class LogicalFlowStressGenerator {

    private static final String PROVENANCE = "stress";
    private static final int HOW_MANY_FLOWS = 100;
    private static final int MAX_DATA_TYPES_PER_FLOW = 4;

    private final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
    private final DSLContext dsl = ctx.getBean(DSLContext.class);
    private final DataTypeDao dtDao = ctx.getBean(DataTypeDao.class);
    private final LogicalFlowDao lfDao = ctx.getBean(LogicalFlowDao.class);


    private final long appId = 655L;

    public static void main(String[] args) {
        new LogicalFlowStressGenerator().go();
    }


    private void go() {
        cleanup();
        generateFlows();
        generateDataTypeMappings();
    }


    private void generateDataTypeMappings() {
        List<DataType> dataTypes = dtDao.findAll();

        List<Long> flowIds = dsl
                .select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.PROVENANCE.eq(PROVENANCE))
                .fetch(LOGICAL_FLOW.ID);

        Set<LogicalFlowDecoratorRecord> decorators = flowIds.stream()
                .flatMap(fId -> RandomUtilities
                        .randomlySizedIntStream(1, MAX_DATA_TYPES_PER_FLOW)
                        .mapToObj(x -> tuple(fId, randomPick(dataTypes))))
                .distinct()
                .map(t -> mkLogicalFlowDecoratorRecord(t.v1, t.v2.entityReference().id(), PROVENANCE))
                .collect(toSet());

        System.out.println(decorators);

        dsl.batchInsert(decorators).execute();
    }


    private void generateFlows() {
        List<Long> appIds = getActiveAppIds(dsl);

        Set<Tuple2<Long, Long>> existingFlows = lfDao
                .findByEntityReference(EntityReference.mkRef(EntityKind.APPLICATION, appId))
                .stream()
                .map(lf -> tuple(lf.source().id(), lf.target().id()))
                .collect(toSet());

        Set<LogicalFlowRecord> flows = randomPick(appIds, HOW_MANY_FLOWS)
                .stream()
                .filter(cId -> cId != appId) // don't create self flows
                .map(cId -> cId % 2 == 0
                        ? tuple(appId, cId)
                        : tuple(cId, appId))
                .filter(t -> ! existingFlows.contains(t))
                .map(t -> mkLogicalFlowRecord(t.v1, t.v2, PROVENANCE))
                .collect(toSet());

        dsl.batchInsert(flows).execute();
    }


    private void cleanup() {
        dsl.deleteFrom(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.PROVENANCE.eq(PROVENANCE))
                .execute();

        dsl.deleteFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.PROVENANCE.eq(PROVENANCE))
                .execute();
    }

}
