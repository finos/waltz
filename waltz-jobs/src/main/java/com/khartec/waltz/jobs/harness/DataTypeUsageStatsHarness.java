/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecoratorStat;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

import static com.khartec.waltz.model.EntityKind.APP_GROUP;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class DataTypeUsageStatsHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDecoratorService logicalFlowDecoratorService = ctx.getBean(LogicalFlowDecoratorService.class);

//        ApplicationIdSelectionOptions applicationIdSelectionOptions = ctx.getBean(ApplicationIdSelectionOptions.class);
//        Set<LogicalFlowDecoratorStat> flowsByDatatypeForEntity = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(ApplicationIdSelectionOptions.mkOpts(mkRef(APPLICATION, 20506)));
        Set<LogicalFlowDecoratorStat> flowsByDatatypeForAppGroup = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(IdSelectionOptions.mkOpts(mkRef(APP_GROUP, 433)));
        Set<LogicalFlowDecoratorStat> flowsByDatatypeForCriticAppsAppGroup = logicalFlowDecoratorService.findFlowsByDatatypeForEntity(IdSelectionOptions.mkOpts(mkRef(APP_GROUP, 491)));


//        System.out.println(flowsByDatatypeForEntity);
        System.out.println(flowsByDatatypeForAppGroup);


        for(int i = 0; i < 5; i++) {
            Set<LogicalFlowDecoratorStat> timeJessTest = FunctionUtilities.time(
                    "jess" + i,
                    () -> logicalFlowDecoratorService.findFlowsByDatatypeForEntity(mkOpts(mkRef(APP_GROUP, 433))));
            FunctionUtilities.time(
                    "crit"+i,
                    () -> logicalFlowDecoratorService.findFlowsByDatatypeForEntity(mkOpts(mkRef(APP_GROUP, 491))));
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
