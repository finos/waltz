package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDefinitionDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.EntityStatisticValue;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatisticValue;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static java.util.stream.Collectors.*;

public class EntityStatisticGenerator implements SampleDataGenerator {

    private static final String PROVENANCE = "DEMO";
    private static final int VALUE_COUNT = 2;


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

        createAdoptionStatsFor(SDLC_TECH, applications, valueDao);
        createAdoptionStatsFor(SDLC_PROCESS, applications, valueDao);
        createAdoptionStatsFor(SDLC_JIRA, applications, valueDao);
        createAdoptionStatsFor(SDLC_SVN, applications, valueDao);
        createAdoptionStatsFor(SDLC_WIKI, applications, valueDao);

        return null;//
//
//
//        // insert new entity stats
//        List<EntityStatisticDefinition> allEntityStatistics = null;
//        try {
//            allEntityStatistics = insertDefinitions(statisticsDefinitionDao);
//        } catch (IOException e) {
//            System.out.println("Failed to insert entity statistics: " + e.getMessage());
//            e.printStackTrace();
//        }
//        System.out.println("Inserted entity statistics: " + allEntityStatistics.size());
//
//        List<EntityStatisticValue> values = new ArrayList<>(applications.length * allEntityStatistics.size());
//        for (Application app : applications) {
//
//            for (EntityStatisticDefinition es : allEntityStatistics) {
//                try {
//                    values.addAll(buildValue(app, es));
//                } catch (IOException e) {
//                    System.out.println("Failed to insert entity statistics: " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        int[] results = statisticsValueDao.bulkSaveValues(values);
//        System.out.println("inserted entity statistic values: " + results.length);
//
//        Map<String, Integer> result = new HashMap<>(2);
//        result.put("EntityStatistic", allEntityStatistics.size());
//        result.put("EntityStatisticValue", results.length);
//        return result;
    }

    private void createAdoptionStatsFor(EntityStatisticDefinition defn,
                                        Application[] applications,
                                        EntityStatisticValueDao valueDao) {

        List<EntityStatisticValue> values = Stream.of(applications)
                .map(app -> app.id().get())
                .map(appId -> {
                    String result = randomPick("COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT");
                    return ImmutableEntityStatisticValue
                            .builder()
                            .entity(ImmutableEntityReference
                                    .builder()
                                    .id(appId)
                                    .kind(EntityKind.APPLICATION)
                                    .build())
                            .current(true)
                            .state(StatisticValueState.PROVIDED)
                            .outcome(result)
                            .value(result)
                            .statisticId(defn.id().get())
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        valueDao.bulkSaveValues(values);
    }


    private static List<EntityStatisticDefinition> insertDefinitions(EntityStatisticDefinitionDao definitionDao) throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/entity-statistics.csv"));
        List<EntityStatisticDefinition> definitions = lines.stream()
                .skip(1)
                .map(line -> line.split(","))
                .filter(cells -> cells.length == 5)
                .map(cells -> ImmutableEntityStatisticDefinition.builder()
                        .name(cells[0])
                        .description("Described: " + cells[0])
                        .type(StatisticType.valueOf(cells[1]))
                        .category(StatisticCategory.valueOf(cells[2]))
                        .active(true)
                        .provenance(PROVENANCE)
                        .renderer(cells[3])
                        .historicRenderer(cells[4])
                        .build())
                .collect(toList());

        definitions.forEach(entityStatistic -> definitionDao.insert(entityStatistic));
        return definitionDao.getAllDefinitions();
    }


    private List<EntityStatisticValue> buildValue(Application app, EntityStatisticDefinition es) throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/entity-statistic-values.csv"));
        Map<StatisticType, List<Tuple4>> statisticTypeValueMap = lines.stream()
                .skip(1)
                .map(line -> line.split(","))
                .filter(cells -> cells.length == 5)
                .map(cells -> {
                    Map<StatisticType, Tuple4> typeToTuple = new HashMap<>();
                    typeToTuple.put(StatisticType.valueOf(cells[0]), new Tuple4(cells[1], cells[2], cells[3], cells[4]));
                    return typeToTuple;
                })
                .flatMap(typeToTuple -> typeToTuple.entrySet().stream())
                .collect(groupingBy(entry -> entry.getKey(), mapping(entry -> entry.getValue(), toList())));

        // create a value
        List<EntityStatisticValue> values = new ArrayList<>(VALUE_COUNT);
        for (int i = 0; i < VALUE_COUNT; i++) {
            List<Tuple4> statisticValues = statisticTypeValueMap.get(es.type());
            Tuple4 esvSample = randomPick(statisticValues.toArray(new Tuple4[statisticValues.size()]));
            ImmutableEntityStatisticValue value = ImmutableEntityStatisticValue.builder()
                    .statisticId(es.id().get())
                    .entity(ImmutableEntityReference.builder()
                            .kind(EntityKind.APPLICATION)
                            .id(app.id().get())
                            .build())
                    .value(esvSample.v2().toString())
                    .outcome(esvSample.v3().toString())
                    .state(StatisticValueState.valueOf(esvSample.v1().toString()))
                    .reason((String) esvSample.v4())
                    .createdAt(DateTimeUtilities.nowUtc().minusDays(i))
                    .current(i == 0)
                    .provenance(PROVENANCE)
                    .build();

            values.add(value);
        }
        return values;
    }
}
