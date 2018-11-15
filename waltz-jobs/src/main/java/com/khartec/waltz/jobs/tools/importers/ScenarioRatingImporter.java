package com.khartec.waltz.jobs.tools.importers;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioAxisItem;
import com.khartec.waltz.schema.tables.records.ScenarioRatingItemRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.StringUtilities.lower;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ScenarioRatingItem.SCENARIO_RATING_ITEM;
import static java.util.stream.Collectors.*;

@Component
public class ScenarioRatingImporter {

    private final DSLContext dsl;
    private final MeasurableDao measurableDao;
    private final RatingSchemeDAO ratingSchemeDao;
    private final RoadmapDao roadmapDao;
    private final ScenarioAxisItemDao scenarioAxisItemDao;
    private final ScenarioDao scenarioDao;


    private Map<String, Application> assetCodeToApplicationMap;


    public ScenarioRatingImporter(ApplicationDao applicationDao,
                                  DSLContext dsl,
                                  MeasurableDao measurableDao,
                                  RatingSchemeDAO ratingSchemeDao,
                                  RoadmapDao roadmapDao,
                                  ScenarioAxisItemDao scenarioAxisItemDao,
                                  ScenarioDao scenarioDao) {
        this.dsl = dsl;
        this.measurableDao = measurableDao;
        this.ratingSchemeDao = ratingSchemeDao;
        this.roadmapDao = roadmapDao;
        this.scenarioAxisItemDao = scenarioAxisItemDao;
        this.scenarioDao = scenarioDao;


        List<Application> allApps = dsl.select()
                .from(APPLICATION)
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
        assetCodeToApplicationMap = indexBy(a -> a.assetCode().get(), allApps);
    }


    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ScenarioRatingImporter importer = ctx.getBean(ScenarioRatingImporter.class);

        String filename = "scenario-rating-import-template.csv";
        importer.importScenarioRatings(filename);
    }


    public void importScenarioRatings(String filename) throws IOException {
        List<ScenarioRatingRow> ratingRows = parseData(filename);

        Map<String, Map<String, List<ScenarioRatingRow>>> rowsGroupedByRoadmapByScenario = ratingRows
                .stream()
                .collect(groupingBy(m -> m.roadmap(), groupingBy(m -> m.scenario())));

        // get roadmap id
        Map<String, Roadmap> roadmapNameToIdMap = getNameToRoadmapMap(roadmapDao);
        List<Measurable> measurables = measurableDao.findAll();

        rowsGroupedByRoadmapByScenario.forEach((String roadmapName, Map<String, List<ScenarioRatingRow>> scenarioAndRows) -> {
            // get roadmap
            Roadmap roadmap = roadmapNameToIdMap.get(lower(roadmapName));
            checkNotNull(roadmap, "roadmap [" + roadmapName + "] cannot be null");

            // get rating scheme
            RatingScheme ratingScheme = ratingSchemeDao.getById(roadmap.ratingSchemeId());
            checkNotNull(ratingScheme, "ratingScheme cannot be null");
            Map<String, RagName> ratingsByName = indexBy(r -> lower(r.name()), ratingScheme.ratings());

            // index available scenarios
            Map<String, Scenario> scenariosByName = indexBy(s -> lower(s.name()), scenarioDao.findForRoadmapId(roadmap.id().get()));

            scenarioAndRows.forEach((scenarioName, rows) ->
                    updateRatingsForScenario(measurables, roadmap, ratingsByName, rows, scenariosByName, scenarioName));

        });

    }


    private void updateRatingsForScenario(List<Measurable> measurables,
                                          Roadmap roadmap,
                                          Map<String, RagName> ratingsByName,
                                          List<ScenarioRatingRow> rows,
                                          Map<String, Scenario> scenariosByName,
                                          String scenarioName) {
        // fetch scenario and axis items
        Scenario scenario = scenariosByName.get(lower(scenarioName));
        checkNotNull(scenario, "scenario [" + scenarioName+ "] cannot be null");

        //ensure all scenario axes
        ensureScenarioAxes(measurables, roadmap, scenario, rows);

        // convert rows to scenario rating items
        List<ScenarioRatingItemRecord> records = mkScenarioRatingRecords(measurables, ratingsByName, rows, scenario);

        removeExistingRatings(dsl, scenario);

        // insert new ones
        int[] inserts = dsl
                .batchInsert(records)
                .execute();
        System.out.printf("Inserted %s records for scenario %s \n", inserts.length, scenarioName);
    }


    private void ensureScenarioAxes(List<Measurable> measurables,
                                    Roadmap roadmap,
                                    Scenario scenario,
                                    List<ScenarioRatingRow> ratingRows) {
        Collection<ScenarioAxisItem> scenarioAxes = scenarioAxisItemDao.findForScenarioId(scenario.id().get());
        Map<Long, Measurable> measurablesById = indexBy(m -> m.id().get(), measurables);
        Map<String, ScenarioAxisItem> scenarioAxesByName = scenarioAxes.stream()
                .map(sa -> Tuple.tuple( lower( measurablesById.get(sa.domainItem().id()).name() ), sa))
                .collect(toMap(t -> t.v1(), t -> t.v2()));

        // check and create any missing columns
        addMissingColumns(measurables, ratingRows, roadmap, scenarioAxisItemDao,  scenarioAxesByName, scenario);
        addMissingRows(measurables, ratingRows, roadmap, scenarioAxisItemDao,  scenarioAxesByName, scenario);
    }


    private void addMissingColumns(List<Measurable> measurables,
                                   List<ScenarioRatingRow> ratingRows,
                                   Roadmap roadmap,
                                   ScenarioAxisItemDao scenarioAxisItemDao,
                                   Map<String, ScenarioAxisItem> scenarioAxesByName,
                                   Scenario scenario) {
        List<String> columns = ratingRows
                .stream()
                .map(r -> lower(r.column()))
                .distinct()
                .collect(toList());

        Map<String, Measurable> allColumnMeasurables = measurables.stream()
                .filter(m -> m.categoryId() == roadmap.columnType().id())
                .collect(toMap(m -> lower(m.name()), m -> m, (m1, m2) -> m2.concrete() ? m2 : m1));

        for (String column : columns) {
            if(!scenarioAxesByName.containsKey(column)) {
                Measurable measurable = allColumnMeasurables.get(column);
                checkNotNull(measurable, String.format("measurable with name [%s] not found", column));
                scenarioAxisItemDao.add(scenario.id().get(), AxisOrientation.COLUMN, measurable.entityReference(), 0);
            }
        }
    }


    private void addMissingRows(List<Measurable> measurables,
                                List<ScenarioRatingRow> ratingRows,
                                Roadmap roadmap,
                                ScenarioAxisItemDao scenarioAxisItemDao,
                                Map<String, ScenarioAxisItem> scenarioAxesByName,
                                Scenario scenario) {
        List<String> rows = ratingRows
                .stream()
                .map(r -> lower(r.row()))
                .distinct()
                .collect(toList());

        Map<String, Measurable> allRowMeasurables = measurables.stream()
                .filter(m -> m.categoryId() == roadmap.rowType().id())
                .collect(toMap(m -> lower(m.name()), m -> m, (m1, m2) -> m2.concrete() ? m2 : m1));

        for (String row : rows) {
            if(!scenarioAxesByName.containsKey(row)) {
                Measurable measurable = allRowMeasurables.get(row);
                checkNotNull(measurable, String.format("measurable with name [%s] not found", row));
                scenarioAxisItemDao.add(scenario.id().get(), AxisOrientation.ROW, measurable.entityReference(), 0);
            }
        }
    }


    private void removeExistingRatings(DSLContext dsl, Scenario scenario) {
        dsl.deleteFrom(SCENARIO_RATING_ITEM)
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(scenario.id().get()))
                .execute();
    }


    private List<ScenarioRatingItemRecord> mkScenarioRatingRecords(List<Measurable> measurables,
                                                                   Map<String, RagName> ratingsByName,
                                                                   List<ScenarioRatingRow> rows,
                                                                   Scenario scenario) {
        Collection<ScenarioAxisItem> scenarioAxes = scenarioAxisItemDao.findForScenarioId(scenario.id().get());
        Map<Long, Measurable> measurablesById = indexBy(m -> m.id().get(), measurables);
        Map<String, ScenarioAxisItem> scenarioAxesByName = scenarioAxes.stream()
                .map(sa -> Tuple.tuple(lower(measurablesById.get(sa.domainItem().id()).name()), sa))
                .collect(toMap(t -> t.v1(), t -> t.v2()));

        return rows.stream()
                .map(r -> {
                    ScenarioAxisItem columnAxis = scenarioAxesByName.get(lower(r.column()));
                    checkNotNull(columnAxis, "columnAxis cannot be null");
                    checkTrue(columnAxis.axisOrientation().equals(AxisOrientation.COLUMN), "column does not match a oolumn axis");

                    ScenarioAxisItem rowAxis = scenarioAxesByName.get(lower(r.row()));
                    checkNotNull(rowAxis, "rowAxis cannot be null");
                    checkTrue(rowAxis.axisOrientation().equals(AxisOrientation.ROW), "row does not match a row axis");

                    Application app = assetCodeToApplicationMap.get(r.assetCode());
                    checkNotNull(app, String.format("Application with asset code[%s] cannot be null", r.assetCode()));

                    RagName rating = ratingsByName.get(lower(r.rating()));
                    checkNotNull(rating, String.format("rating [%s] cannot be null", r.rating()));

                    ScenarioRatingItemRecord record = new ScenarioRatingItemRecord();
                    record.setScenarioId(scenario.id().get());
                    record.setRating(rating.rating().toString());
                    record.setDomainItemKind(EntityKind.APPLICATION.name());
                    record.setDomainItemId(app.id().get());
                    record.setRowKind(rowAxis.domainItem().kind().name());
                    record.setRowId(rowAxis.domainItem().id());
                    record.setColumnKind(columnAxis.domainItem().kind().name());
                    record.setColumnId(columnAxis.domainItem().id());
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.setLastUpdatedBy(r.providedBy());
                    record.setDescription(r.description());
                    return record;
                })
                .collect(toList());
    }


    private Map<String, Roadmap> getNameToRoadmapMap(RoadmapDao roadmapDao) {
        return roadmapDao.findAllActive()
                .stream()
                .collect(toMap(r -> lower(r.name()), r -> r));
    }


    private List<ScenarioRatingRow> parseData(String filename) throws IOException {
        InputStreamReader reader = new InputStreamReader(ScenarioRatingImporter.class.getClassLoader().getResourceAsStream(filename));

        CsvPreference prefs = new CsvPreference.Builder('"', ',', "\n")
                .ignoreEmptyLines(true)
                .build();

        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(reader, prefs);

            final String[] header = mapReader.getHeader(true);

            List<ScenarioRatingRow> roadmapRows = new ArrayList<>();
            Map<String, String> row;
            while((row = mapReader.read(header)) != null) {
                roadmapRows.add(com.khartec.waltz.jobs.tools.importers.ImmutableScenarioRatingRow.builder()
                        .roadmap(row.get("Roadmap"))
                        .scenario(row.get("Scenario"))
                        .column(row.get("Column"))
                        .row(row.get("Row"))
                        .assetCode(row.get("Asset Code"))
                        .rating(row.get("Rating"))
                        .description(row.get("Description"))
                        .providedBy(row.get("Last Updated By"))
                        .build());

            }
            return roadmapRows;
        } finally {
            if(mapReader != null) {
                mapReader.close();
                mapReader = null;
            }
        }
    }

}
