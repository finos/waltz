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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_statistic.*;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static java.util.stream.Collectors.toList;

public class EntityStatisticGenerator implements SampleDataGenerator {


    private static final Random rnd = RandomUtilities.getRandom();


    private static final List<LocalDateTime> DATES = IntStream.range(0, 60)
                .mapToObj(i -> LocalDateTime.now().minusDays(i))
                .collect(toList());


    private static final BiFunction<StatisticValueState, Integer, String> failIfPositiveFn = (s, v)
            -> s == StatisticValueState.PROVIDED
            ? (v == 0
                ? "PASS"
                : "FAIL")
            : "N/A";


    private static final EntityStatisticDefinition BASE_DEFN = ImmutableEntityStatisticDefinition.builder()
            .active(true)
            .category(StatisticCategory.COMPLIANCE)
            .description("description")
            .type(StatisticType.ENUM)
            .renderer("enum")
            .historicRenderer("enum")
            .name("dummy stat")
            .entityVisibility(true)
            .rollupVisibility(true)
            .provenance(SAMPLE_DATA_PROVENANCE)
            .build();


    private static final EntityStatisticDefinition AUDIT = ImmutableEntityStatisticDefinition.builder()
            .active(true)
            .id(20000L)
            .category(StatisticCategory.SECURITY)
            .name("Open Audit Issues")
            .description("Audit stuff and things")
            .type(StatisticType.NUMERIC)
            .renderer("bar")
            .historicRenderer("bar")
            .entityVisibility(true)
            .rollupVisibility(true)
            .provenance(SAMPLE_DATA_PROVENANCE)
            .build();


    private static final EntityStatisticDefinition SERVER_COUNT = ImmutableEntityStatisticDefinition.builder()
            .active(true)
            .id(20010L)
            .category(StatisticCategory.SECURITY)
            .name("Server Type Count")
            .description("Server Types")
            .type(StatisticType.NUMERIC)
            .rollupKind(RollupKind.SUM_BY_VALUE)
            .renderer("bar")
            .historicRenderer("bar")
            .entityVisibility(true)
            .rollupVisibility(true)
            .provenance(SAMPLE_DATA_PROVENANCE)
            .build();


    private static final EntityStatisticDefinition SDLC = ImmutableEntityStatisticDefinition
            .copyOf(BASE_DEFN)
            .withId(10000L)
            .withName("SDLC Compliance");

    private static final EntityStatisticDefinition SDLC_TECH = ImmutableEntityStatisticDefinition
            .copyOf(SDLC)
            .withId(10100L)
            .withParentId(10000L)
            .withName("SDLC Technology");

    private static final EntityStatisticDefinition SDLC_PROCESS = ImmutableEntityStatisticDefinition
            .copyOf(SDLC)
            .withId(10200L)
            .withParentId(10000L)
            .withName("SDLC Process");

    private static final EntityStatisticDefinition SDLC_JIRA = ImmutableEntityStatisticDefinition
            .copyOf(SDLC)
            .withId(10101L)
            .withParentId(10100L)
            .withName("SDLC Tech Jira");

    private static final EntityStatisticDefinition SDLC_SVN = ImmutableEntityStatisticDefinition
            .copyOf(SDLC)
            .withId(10102L)
            .withParentId(10100L)
            .withName("SDLC Tech SVN");

    private static final EntityStatisticDefinition SDLC_WIKI = ImmutableEntityStatisticDefinition
            .copyOf(SDLC)
            .withId(10103L)
            .withParentId(10100L)
            .withName("SDLC Tech Wiki");

    private static final EntityStatisticDefinition PRE_COMPUTED = ImmutableEntityStatisticDefinition
            .copyOf(BASE_DEFN)
            .withId(11000L)
            .withRollupKind(RollupKind.NONE)
            .withName("Pre Computed");



    public Map<String, Integer> create(ApplicationContext context) {

        DSLContext dsl = context.getBean(DSLContext.class);
        ApplicationDao applicationDao = context.getBean(ApplicationDao.class);
        OrganisationalUnitDao organisationalUnitDao = context.getBean(OrganisationalUnitDao.class);
        EntityStatisticValueDao valueDao = context.getBean(EntityStatisticValueDao.class);
        EntityStatisticDefinitionDao definitionDao = context.getBean(EntityStatisticDefinitionDao.class);
        EntityHierarchyService entityHierarchyService = context.getBean(EntityHierarchyService.class);

        Application[] applications = applicationDao.findAll().toArray(new Application[0]);
        OrganisationalUnit[] orgUnits = organisationalUnitDao.findAll().toArray(new OrganisationalUnit[0]);

        dsl.deleteFrom(ENTITY_STATISTIC_DEFINITION)
                .where(ENTITY_STATISTIC_DEFINITION.PROVENANCE.eq("DEMO"))
                .execute();

        log("deleted existing statistics (provenance: '" + SAMPLE_DATA_PROVENANCE + "')");

        dsl.update(ENTITY_STATISTIC_VALUE)
                .set(ENTITY_STATISTIC_VALUE.CURRENT, false)
                .where(ENTITY_STATISTIC_VALUE.PROVENANCE.eq("DEMO"))
                .execute();

        log("marked existing statistic values as non-current (provenance: '" + SAMPLE_DATA_PROVENANCE + "')");

        definitionDao.insert(SDLC);
        definitionDao.insert(SDLC_TECH);
        definitionDao.insert(SDLC_PROCESS);
        definitionDao.insert(SDLC_JIRA);
        definitionDao.insert(SDLC_SVN);
        definitionDao.insert(SDLC_WIKI);
        definitionDao.insert(AUDIT);
        definitionDao.insert(SERVER_COUNT);
        definitionDao.insert(PRE_COMPUTED);

        createAdoptionStatsFor(SDLC_TECH, applications, valueDao);
        createAdoptionStatsFor(SDLC_PROCESS, applications, valueDao);
        createAdoptionStatsFor(SDLC_JIRA, applications, valueDao);
        createAdoptionStatsFor(SDLC_SVN, applications, valueDao);
        createAdoptionStatsFor(SDLC_WIKI, applications, valueDao);
        createIntStatsFor(AUDIT, applications, valueDao, 20, failIfPositiveFn);
        createIntStatsFor(SDLC, applications, valueDao, 20, failIfPositiveFn);

        createIntStatsFor(SERVER_COUNT, applications, valueDao, 20, (x, y) -> "VIRTUAL");
        createIntStatsFor(SERVER_COUNT, applications, valueDao, 20, (x, y) -> "BARE_METAL");

        createPreComputedStatsFor(PRE_COMPUTED, orgUnits, valueDao);

        entityHierarchyService.buildFor(EntityKind.ENTITY_STATISTIC);

        log("Rebuilt entity hierarchy");

        return null;
    }

    private void createIntStatsFor(EntityStatisticDefinition defn,
                                   Application[] applications,
                                   EntityStatisticValueDao valueDao,
                                   int bound,
                                   BiFunction<StatisticValueState, Integer, String> outcomeFn) {

        Random rnd = RandomUtilities.getRandom();

        List<EntityStatisticValue> values = streamAppRefs(applications)
                .map(appRef -> {

                    StatisticValueState state = randomPick(StatisticValueState.values());

                    int v = state == StatisticValueState.PROVIDED
                            ? rnd.nextInt(bound)
                            : 0;

                    // naughty
                    v = rnd.nextInt(bound);

                    return ImmutableEntityStatisticValue.builder()
                            .entity(appRef)
                            .state(state)
                            .statisticId(defn.id().get())
                            .current(true)
                            .createdAt(LocalDateTime.now())
                            .value(Integer.toString(v))
                            .outcome(outcomeFn.apply(state, v))
                            .provenance(SAMPLE_DATA_PROVENANCE)
                            .build();
                })
                .collect(toList());

        valueDao.bulkSaveValues(values);
    }


    private void createAdoptionStatsFor(EntityStatisticDefinition defn,
                                        Application[] applications,
                                        EntityStatisticValueDao valueDao) {

        List<EntityStatisticValue> values = streamAppRefs(applications)
                .flatMap(appRef -> {
                    return DATES.stream()
                            .map(d -> {
                                String result = randomPick("COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT");
                                return ImmutableEntityStatisticValue
                                        .builder()
                                        .entity(appRef)
                                        .current(d.toLocalDate().equals(LocalDate.now()))
                                        .state(StatisticValueState.PROVIDED)
                                        .outcome(result)
                                        .value(result)
                                        .statisticId(defn.id().get())
                                        .createdAt(d)
                                        .provenance(SAMPLE_DATA_PROVENANCE)
                                        .build();
                            });

                })
                .collect(Collectors.toList());

        valueDao.bulkSaveValues(values);
    }


    private void createPreComputedStatsFor(EntityStatisticDefinition defn,
                                           OrganisationalUnit[] orgUnits,
                                           EntityStatisticValueDao valueDao) {


        List<EntityStatisticValue> values = streamOrgUnitRefs(orgUnits)
                .map(appRef -> {
                    String result = randomPick("COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT");
                    return ImmutableEntityStatisticValue
                            .builder()
                            .entity(appRef)
                            .current(true)
                            .state(StatisticValueState.PROVIDED)
                            .outcome(result)
                            .value(String.valueOf(rnd.nextInt(5000)))
                            .statisticId(defn.id().get())
                            .createdAt(LocalDateTime.now())
                            .provenance(SAMPLE_DATA_PROVENANCE)
                            .build();
                })
                .collect(Collectors.toList());

        valueDao.bulkSaveValues(values);
    }


    private Stream<EntityReference> streamAppRefs(Application... apps ) {
        return Stream.of(apps)
                .map(app -> app.id().get())
                .map(id -> ImmutableEntityReference
                        .builder()
                        .id(id)
                        .kind(EntityKind.APPLICATION)
                        .build());
    }


    private Stream<EntityReference> streamOrgUnitRefs(OrganisationalUnit... orgs) {
        return Stream.of(orgs)
                .map(org -> org.id().get())
                .map(id -> ImmutableEntityReference
                        .builder()
                        .id(id)
                        .kind(EntityKind.ORG_UNIT)
                        .build());
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(ENTITY_STATISTIC_DEFINITION)
                .where(ENTITY_STATISTIC_DEFINITION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        new EntityStatisticGenerator().create(ctx);
    }
}
