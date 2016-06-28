package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_statistic.EntityStatisticDao;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static java.util.stream.Collectors.*;

public class EntityStatisticGenerator implements SampleDataGenerator {

    private static final String PROVENANCE = "waltz";
    private static final int VALUE_COUNT = 2;


    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        new EntityStatisticGenerator().apply(ctx);
    }


    @Override
    public Map<String, Integer> apply(ApplicationContext context) {

        DSLContext dsl = context.getBean(DSLContext.class);
        ApplicationDao applicationDao = context.getBean(ApplicationDao.class);
        EntityStatisticDao entityStatisticDao = context.getBean(EntityStatisticDao.class);

        Application[] applications = applicationDao.getAll().toArray(new Application[0]);


        dsl.deleteFrom(ENTITY_STATISTIC_DEFINITION).execute();
        dsl.deleteFrom(ENTITY_STATISTIC_VALUE).execute();
        System.out.println("deleted existing statistics");

        // insert new entity stats
        List<EntityStatisticDefinition> allEntityStatistics = null;
        try {
            allEntityStatistics = insertEntityStatistics(entityStatisticDao);
        } catch (IOException e) {
            System.out.println("Failed to insert entity statistics: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Inserted entity statistics: " + allEntityStatistics.size());

        List<EntityStatisticValue> values = new ArrayList<>(applications.length * allEntityStatistics.size());
        for (Application app : applications) {

            for (EntityStatisticDefinition es : allEntityStatistics) {
                try {
                    values.addAll(buildEntityStatisticValues(app, es));
                } catch (IOException e) {
                    System.out.println("Failed to insert entity statistics: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        int[] results = entityStatisticDao.bulkSaveValues(values);
        System.out.println("inserted entity statistic values: " + results.length);

        Map<String, Integer> result = new HashMap<>(2);
        result.put("EntityStatistic", allEntityStatistics.size());
        result.put("EntityStatisticValue", results.length);
        return result;
    }


    private static List<EntityStatisticDefinition> insertEntityStatistics(EntityStatisticDao entityStatisticDao) throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/entity-statistics.csv"));
        List<EntityStatisticDefinition> entityStatistics = lines.stream()
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

        entityStatistics.forEach(entityStatistic -> entityStatisticDao.addEntityStatistic(entityStatistic));
        return entityStatisticDao.getAllEntityStatistics();
    }


    private List<EntityStatisticValue> buildEntityStatisticValues(Application app, EntityStatisticDefinition es) throws IOException {
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
