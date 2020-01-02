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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

/**
 * Created by dwatkins on 13/05/2016.
 */
public class ApplicationIdSelectorHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();
        DSLContext dsl = ctx.getBean(DSLContext.class);

        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        IdSelectionOptions options = mkOpts(
                EntityReference.mkRef(EntityKind.DATA_TYPE, 5000L),
                HierarchyQueryScope.CHILDREN);


        Select<Record1<Long>> selector = factory.apply(options);

        dsl.settings().withRenderFormatted(true);
        List<Application> apps = applicationService.findByAppIdSelector(options);

        System.out.println("--- sz: "+apps.size());
        apps.forEach(System.out::println);


        System.out.println("--- done");
    }


}
