/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

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

        searchService.search("desk", searchOptions).forEach(d -> System.out.println(d.name()));

        System.exit(-1);
        Arrays.stream(searchTerms)
                .forEach(term -> {
                    System.out.println("-------------------");
                    System.out.printf("Searching for '%s'\n", term);
                    System.out.println("-------------------");
                    printResults(searchService.search(term, searchOptions));
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
