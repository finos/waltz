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

import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.roadmap.RoadmapService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class RoadmapHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        RoadmapDao roadmapDao = ctx.getBean(RoadmapDao.class);
        RoadmapService roadmapService = ctx.getBean(RoadmapService.class);

        Collection<RoadmapAndScenarioOverview> relns = roadmapService
                .findRoadmapsAndScenariosByFormalRelationship(mkRef(EntityKind.ORG_UNIT, 2700));

        System.out.println(relns);


    }

}
