package com.khartec.waltz.jobs;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_search.EntitySearchService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

public class EntitySearchHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        EntitySearchService searchService = ctx.getBean(EntitySearchService.class);

        EntitySearchOptions searchOptions = ImmutableEntitySearchOptions.builder()
                .entityKinds(ListUtilities.newArrayList(
                        EntityKind.ACTOR,
                        EntityKind.APPLICATION,
                        EntityKind.CHANGE_INITIATIVE,
                        EntityKind.ORG_UNIT,
                        EntityKind.MEASURABLE,
                        EntityKind.PERSON))
                .build();

        System.out.println("-------------------");
        System.out.println("Searching for 'cat'");
        System.out.println("-------------------");
        printResults(searchService.search("cat", searchOptions));

        System.out.println("-------------------");
        System.out.println("Searching for 'admin'");
        System.out.println("-------------------");
        printResults(searchService.search("admin", searchOptions));

        System.out.println("-------------------");
        System.out.println("Searching for 'enhance'");
        System.out.println("-------------------");
        printResults(searchService.search("enhance", searchOptions));

        System.out.println("-------------------");
        System.out.println("Searching for 'ceo'");
        System.out.println("-------------------");
        printResults(searchService.search("ceo", searchOptions));


        System.out.println("-------------------");
        System.out.println("Searching for 'equities'");
        System.out.println("-------------------");
        printResults(searchService.search("equities", searchOptions));

    }


    private static void printResults(List<EntityReference> results) {
        System.out.println("# of results: " + results.size());
        results.stream()
                .collect(Collectors.groupingBy(EntityReference::kind))
                .entrySet()
                .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue().size()));
    }
}
