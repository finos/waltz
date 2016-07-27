package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_statistic.*;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static java.util.stream.Collectors.toList;

public class EntityStatisticGenerator implements SampleDataGenerator {

    private static final String PROVENANCE = "DEMO";


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
            .provenance(PROVENANCE)
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
            .name("Open Audit Issues")
            .provenance(PROVENANCE)
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


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        new EntityStatisticGenerator().apply(ctx);
    }


    @Override
    public Map<String, Integer> apply(ApplicationContext context) {

        DSLContext dsl = context.getBean(DSLContext.class);
        ApplicationDao applicationDao = context.getBean(ApplicationDao.class);
        EntityStatisticValueDao valueDao = context.getBean(EntityStatisticValueDao.class);
        EntityStatisticDefinitionDao definitionDao = context.getBean(EntityStatisticDefinitionDao.class);

        Application[] applications = applicationDao.getAll().toArray(new Application[0]);

        dsl.deleteFrom(ENTITY_STATISTIC_DEFINITION)
                .where(ENTITY_STATISTIC_DEFINITION.PROVENANCE.eq("DEMO"))
                .execute();

        dsl.deleteFrom(ENTITY_STATISTIC_VALUE)
                .where(ENTITY_STATISTIC_VALUE.PROVENANCE.eq("DEMO"))
                .execute();

        System.out.println("deleted existing statistics (provenance: '" + PROVENANCE + "')");

        definitionDao.insert(SDLC);
        definitionDao.insert(SDLC_TECH);
        definitionDao.insert(SDLC_PROCESS);
        definitionDao.insert(SDLC_JIRA);
        definitionDao.insert(SDLC_SVN);
        definitionDao.insert(SDLC_WIKI);
        definitionDao.insert(AUDIT);

        createAdoptionStatsFor(SDLC_TECH, applications, valueDao);
        createAdoptionStatsFor(SDLC_PROCESS, applications, valueDao);
        createAdoptionStatsFor(SDLC_JIRA, applications, valueDao);
        createAdoptionStatsFor(SDLC_SVN, applications, valueDao);
        createAdoptionStatsFor(SDLC_WIKI, applications, valueDao);
        createIntStatsFor(AUDIT, applications, valueDao, 20, failIfPositiveFn);

        return null;
    }

    private void createIntStatsFor(EntityStatisticDefinition defn,
                                   Application[] applications,
                                   EntityStatisticValueDao valueDao,
                                   int bound,
                                   BiFunction<StatisticValueState, Integer, String> outcomeFn) {

        Random rnd = new Random(System.currentTimeMillis());

        List<EntityStatisticValue> values = streamAppRefs(applications)
                .map(appRef -> {

                    StatisticValueState state = randomPick(StatisticValueState.values());

                    int v = state == StatisticValueState.PROVIDED
                            ? rnd.nextInt(bound)
                            : 0;

                    return ImmutableEntityStatisticValue.builder()
                            .entity(appRef)
                            .state(state)
                            .statisticId(defn.id().get())
                            .current(true)
                            .createdAt(LocalDateTime.now())
                            .value(Integer.toString(v))
                            .outcome(outcomeFn.apply(state, v))
                            .provenance(PROVENANCE)
                            .build();
                })
                .collect(toList());

        valueDao.bulkSaveValues(values);
    }


    private void createAdoptionStatsFor(EntityStatisticDefinition defn,
                                        Application[] applications,
                                        EntityStatisticValueDao valueDao) {

        List<EntityStatisticValue> values = streamAppRefs(applications)
                .map(appRef -> {
                    String result = randomPick("COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT");
                    return ImmutableEntityStatisticValue
                            .builder()
                            .entity(appRef)
                            .current(true)
                            .state(StatisticValueState.PROVIDED)
                            .outcome(result)
                            .value(result)
                            .statisticId(defn.id().get())
                            .createdAt(LocalDateTime.now())
                            .provenance(PROVENANCE)
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


}
