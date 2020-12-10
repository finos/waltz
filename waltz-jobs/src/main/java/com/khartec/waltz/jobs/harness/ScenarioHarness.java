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

import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.scenario.ScenarioService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

import static com.khartec.waltz.common.ListUtilities.newArrayList;


public class ScenarioHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        ScenarioService scenarioService = ctx.getBean(ScenarioService.class);
        ScenarioAxisItemDao scenarioAxisItemDao = ctx.getBean(ScenarioAxisItemDao.class);

        int[] result = scenarioAxisItemDao
                .reorder(
                    22L,
                    AxisOrientation.ROW,
                    newArrayList(118L, 161L));

        System.out.println(Arrays.toString(result));

    }

}
