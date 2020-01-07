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
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class MsSqlSearchHarness {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        SqlServerAppSearch appSearch = new SqlServerAppSearch();

        EntitySearchOptions searchOptions = ImmutableEntitySearchOptions.builder()
                .addEntityKinds(EntityKind.APPLICATION)
                .userId("admin")
                .limit(EntitySearchOptions.DEFAULT_SEARCH_RESULTS_LIMIT)
                .searchQuery("sap")
                .build();

        List<Application> results = appSearch.searchFullText(
                dsl,
                searchOptions);

        results.stream()
            .filter(a -> a.entityLifecycleStatus() != EntityLifecycleStatus.REMOVED)
            .forEach(a -> System.out.println(a.name() + " - " + a.lifecyclePhase()));
    }


}
