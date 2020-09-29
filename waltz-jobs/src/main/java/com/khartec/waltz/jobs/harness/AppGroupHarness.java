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

import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.app_group.AppGroupEntry;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.app_group.FavouritesService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;


public class AppGroupHarness {

    public static void main(String[] args) throws InsufficientPrivelegeException {
        System.out.println("--- start");

        ApplicationIdSelectorFactory appSelectorFactory = new ApplicationIdSelectorFactory();

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        FavouritesService service = ctx.getBean(FavouritesService.class);

        Collection<AppGroupEntry> appGroupEntries = service.addApplication("jessica.woodland-scott@db.com", 15792);

        System.out.println("--- done");
    }
}
