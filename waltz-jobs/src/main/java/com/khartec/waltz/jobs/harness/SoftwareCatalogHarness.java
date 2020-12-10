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

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.software_catalog.SoftwareSummaryStatistics;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.software_catalog.SoftwareCatalogService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class SoftwareCatalogHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SoftwareCatalogService softwareCatalogService = ctx.getBean(SoftwareCatalogService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        EntityReference ref = ImmutableEntityReference.builder()
                .kind(EntityKind.ORG_UNIT)
                .id(20L)
                .build();

        IdSelectionOptions options = IdSelectionOptions.mkOpts(ref, HierarchyQueryScope.CHILDREN);

        SoftwareSummaryStatistics stats = softwareCatalogService.calculateStatisticsForAppIdSelector(options);
        System.out.println("stats:"+stats);
    }




}
