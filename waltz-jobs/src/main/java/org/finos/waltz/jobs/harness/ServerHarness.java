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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.server_information.ServerInformationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.service.DIBaseConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class ServerHarness {

    public static void main(String[] args) {

        System.out.println("start");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);
        ServerInformationDao serverInfoDao = ctx.getBean(ServerInformationDao.class);


        IdSelectionOptions mOpts = mkOpts(
                mkRef(EntityKind.MEASURABLE, 1),
                HierarchyQueryScope.CHILDREN);

        IdSelectionOptions pOpts = mkOpts(
                mkRef(EntityKind.PERSON, 2),
                HierarchyQueryScope.CHILDREN);

        IdSelectionOptions ouOpts = mkOpts(
                mkRef(EntityKind.ORG_UNIT, 3),
                HierarchyQueryScope.CHILDREN);

        IdSelectionOptions agOpts = mkOpts(
                mkRef(EntityKind.APP_GROUP, 4),
                HierarchyQueryScope.EXACT);


        System.out.println("start timer");
        ListUtilities.asList(mOpts, pOpts, ouOpts, agOpts)
                .forEach(opts -> {
                    FunctionUtilities.time("stats: " + opts.entityReference(), () -> {
                        Select<Record1<Long>> selector = new ApplicationIdSelectorFactory().apply(opts);
                        return serverInfoDao.calculateStatsForAppSelector(selector);
                    });
                });
        System.out.println("end");

    }




}
