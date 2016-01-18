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

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.model.dataflow.RatedDataFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow.RatedDataFlowService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;


public class RatedFlowHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        RatedDataFlowService ratedDataFlowService = ctx.getBean(RatedDataFlowService.class);

        int CTO_OFFICE = 40;
        int CTO_ADMIN = 400;
        int CEO_OFFICE = 10;
        int MARKET_RISK = 220;
        int RISK = 210;

        int orgUnitId = RISK;


        tree(ratedDataFlowService, orgUnitId);

    }


    private static void tree(RatedDataFlowService ratedDataFlowService, int orgUnitId) {
        Collection<RatedDataFlow> ratedFlowsTree = ratedDataFlowService.calculateRatedFlowsForOrgUnitTree(orgUnitId);
        System.out.println("Tree");
        System.out.println(MapUtilities.countBy(rf -> rf.rating(), ratedFlowsTree));
        System.out.println();
    }


}
