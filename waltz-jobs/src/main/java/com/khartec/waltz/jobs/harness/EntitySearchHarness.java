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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_search.EntitySearchService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySearchHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        EntitySearchService searchService = ctx.getBean(EntitySearchService.class);

        EntitySearchOptions searchOptions = ImmutableEntitySearchOptions.builder()
                .entityKinds(ListUtilities.newArrayList(
//                        EntityKind.APPLICATION,
                        EntityKind.LOGICAL_DATA_ELEMENT))
                .searchQuery("desk")
                .userId("admin")
                .build();

        String[] searchTerms = {
                "cat",
                "admin",
                "test",
                "enhance",
                "ceo",
                "equities"
        };

        searchService.search(searchOptions).forEach(d -> System.out.println(d.name()));

        System.exit(-1);
        Arrays.stream(searchTerms)
                .forEach(term -> {
                    System.out.println("-------------------");
                    System.out.printf("Searching for '%s'\n", term);
                    System.out.println("-------------------");
                    printResults(searchService.search(searchOptions));
                });
    }


    private static void printResults(List<EntityReference> results) {
        System.out.println("# of results: " + results.size());
        results.stream()
                .collect(Collectors.groupingBy(EntityReference::kind))
                .entrySet()
                .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue().size()));
    }
}
