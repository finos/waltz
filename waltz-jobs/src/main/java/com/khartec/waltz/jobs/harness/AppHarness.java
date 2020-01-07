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

import com.khartec.waltz.data.application.search.SqlServerAppSearch;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.map;


public class AppHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        // Water & Vole
        // P&S Blotter
        // P & S Gorilla

        List<Application> jimmy = new SqlServerAppSearch()
                .searchFullText(dsl, EntitySearchOptions.mkForEntity(EntityKind.APPLICATION, "Water & Vole"));

        System.out.println(jimmy);
//        ApplicationService applicationService = ctx.getBean(ApplicationService.class);
//        DSLContext dsl = ctx.getBean(DSLContext.class);
//
//        List<String> tagList = applicationService.findAllTags();
//
//        tagList.forEach(System.out::println);
//
//        System.out.println("---------------");
//
//        applicationService.findByTag("not-good-at-flying").forEach(a -> System.out.println(a.name()));
//
//        System.out.println(applicationService.findTagsForApplication(521L));
//

    }


    private static void prettyPrint(Map<AssetCodeRelationshipKind, List<Application>> grouped) {
        grouped.forEach((key, apps) ->
                System.out.println(key.name() + map(apps, relatedApp -> "\n\t"+ toString(relatedApp))));
    }


    private static String toString(Application app) {
        return app.name() + " " + app.assetCode() + " / " + app.parentAssetCode();
    }

}
