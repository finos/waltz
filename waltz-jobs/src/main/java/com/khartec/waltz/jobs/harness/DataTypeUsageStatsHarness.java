/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecoratorStat;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

import static com.khartec.waltz.model.EntityKind.APP_GROUP;
import static com.khartec.waltz.model.EntityReference.mkRef;


public class DataTypeUsageStatsHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDecoratorService logicalFlowDecoratorService = ctx.getBean(LogicalFlowDecoratorService.class);

//        ApplicationIdSelectionOptions applicationIdSelectionOptions = ctx.getBean(ApplicationIdSelectionOptions.class);
//        Set<LogicalFlowDecoratorStat> flowsByDatatypeForEntity = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APPLICATION, 20506)));
        Set<LogicalFlowDecoratorStat> flowsByDatatypeForAppGroup = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APP_GROUP, 433)));
        Set<LogicalFlowDecoratorStat> flowsByDatatypeForCriticAppsAppGroup = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APP_GROUP, 491)));


//        System.out.println(flowsByDatatypeForEntity);
        System.out.println(flowsByDatatypeForAppGroup);


        for(int i = 0; i < 5; i++) {
            Set<LogicalFlowDecoratorStat> timeJessTest = FunctionUtilities.time(
                    "jess" + i,
                    () -> logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APP_GROUP, 433))));
            FunctionUtilities.time(
                    "crit"+i,
                    () -> logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APP_GROUP, 491))));
//            FunctionUtilities.time(
//                    "group"+i,
//                    () -> logicalFlowDecoratorService.findFlowsByDatatypeForEntity(mkRef(ORG_UNIT, 95)));
            dump("jessTest", timeJessTest);
        }

//        dump("entity", flowsByDatatypeForEntity);
//        dump("justWaltz", flowsByDatatypeForAppGroup);

        //for each datatype:
        //findParents();
        //findChildren();
        //fetch results from usageStatsForAllDatatypes
        //AddSetsTogether
        //Return dtId and count

    }

    private static void dump(String name, Set<LogicalFlowDecoratorStat> data) {
        System.out.println("--- " + name + " -----");
        data.stream()
                .forEach(System.out::println);
    }


}
