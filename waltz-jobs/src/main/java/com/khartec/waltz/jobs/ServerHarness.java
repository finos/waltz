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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.server_info.ServerInfoDao;
import com.khartec.waltz.model.dataflow.RatedDataFlow;
import com.khartec.waltz.model.serverinfo.ServerSummaryStatistics;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.ServerInformation;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow.RatedDataFlowService;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;


public class ServerHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ServerInfoDao serverInfoDao = ctx.getBean(ServerInfoDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        dsl.select(APPLICATION.ORGANISATIONAL_UNIT_ID,
                    DSL.coalesce(SERVER_INFORMATION.IS_VIRTUAL, Boolean.FALSE),
                    DSL.countDistinct(SERVER_INFORMATION.HOSTNAME))
                .from(SERVER_INFORMATION)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.in(150L, 140L))
                .where(SERVER_INFORMATION.ASSET_CODE.eq(APPLICATION.ASSET_CODE))
                .groupBy(SERVER_INFORMATION.IS_VIRTUAL, APPLICATION.ORGANISATIONAL_UNIT_ID)
                .fetch()
                .forEach(System.out::println);


        System.out.println("-------------");

        ServerSummaryStatistics stats = serverInfoDao.findStatsForAppIds(new Long[]{7L});
        System.out.println(stats);
    }


    private static void tree(RatedDataFlowService ratedDataFlowService, int orgUnitId) {
        Collection<RatedDataFlow> ratedFlowsTree = ratedDataFlowService.calculateRatedFlowsForOrgUnitTree(orgUnitId);
        System.out.println("Tree");
        System.out.println(MapUtilities.countBy(rf -> rf.rating(), ratedFlowsTree));
        System.out.println();
    }


}
