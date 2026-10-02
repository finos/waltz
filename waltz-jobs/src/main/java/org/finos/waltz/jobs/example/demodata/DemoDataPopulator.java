package org.finos.waltz.jobs.example.demodata;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.jobs.example.demodata.generators.AllocationAndPrimaryRatingGenerator;
import org.finos.waltz.jobs.example.demodata.loaders.*;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class DemoDataPopulator {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataPopulator.class);


    private final DSLContext dsl;
    private final EntityHierarchyService hierarchyService;


    public DemoDataPopulator(DSLContext dsl, EntityHierarchyService hierarchyService) {
        this.dsl = dsl;
        this.hierarchyService = hierarchyService;
    }



    private void go() throws IOException {
        Workbook workbook = WorkbookFactory.create(IOUtilities.getFileResource("sample-data/v1.xlsx").getInputStream());
        dsl.transaction(ctx -> {
            DSLContext waltz = ctx.dsl();
            Timestamp now = DateTimeUtilities.nowUtcTimestamp();

            blat(waltz);

            Tuple2<Long, Map<InvestmentRating, Long>> categoryAndScheme = createCategoryAndScheme(waltz, now);

            OrgUnitLoader.process(waltz, workbook.getSheet("org"), now);
            ApplicationLoader.process(waltz, workbook.getSheet("apps"), now);
            DataTypeLoader.process(waltz, workbook.getSheet("data-types"), now);
//            DataFlowLoader.process(waltz, workbook.getSheet("flows"), now);
            CapabilityLoader.process(waltz, workbook.getSheet("capabilities"), categoryAndScheme.v1, now);
            AppToCapabilityLoader.process(waltz, workbook.getSheet("app-cap"), now);
            AppToAssessmentLoader.process(waltz, workbook.getSheet("app-assessments"), now);
            AllocationAndPrimaryRatingGenerator.generate(waltz, categoryAndScheme.v1);



            rebuildHierarchies();


            throw new RuntimeException("rolling back, comment this line if you really want to do this!");
        });

        LOG.debug("Done");
    }


    private void rebuildHierarchies() {
        LOG.debug("Rebuilding hierarchies");
        hierarchyService.buildFor(EntityKind.ORG_UNIT);
        hierarchyService.buildFor(EntityKind.MEASURABLE);
        hierarchyService.buildFor(EntityKind.PERSON);
        hierarchyService.buildFor(EntityKind.DATA_TYPE);
    }








    private Tuple2<Long, Map<InvestmentRating, Long>> createCategoryAndScheme(DSLContext waltz,
                                                                              Timestamp now) {
        long rsId = createRatingScheme(waltz);
        Map<InvestmentRating, Long> ratingToIdMap = createRatingSchemeItems(waltz, rsId);
        long mcId = createCategory(waltz, rsId, now);
        return tuple(mcId, ratingToIdMap);
    }


    private Map<InvestmentRating, Long> createRatingSchemeItems(DSLContext waltz, long rsId) {
        Set<RatingSchemeItemRecord> items = Stream
                .of(InvestmentRating.values())
                .map(d -> {
                    RatingSchemeItemRecord r = waltz.newRecord(rsi);
                    r.setName(d.displayName);
                    r.setColor(d.color);
                    r.setDescription(format(
                            "Investment Rating: %s",
                            d.displayName));
                    r.setExternalId(d.name());
                    r.setSchemeId(rsId);
                    r.setCode(d.code);
                    return r;
                })
                .collect(toSet());

        int insertCount = summarizeResults(waltz.batchInsert(items).execute());

        LOG.debug("Created {} new ratings for scheme {}", insertCount, rsId);

        return indexBy(
                items,
                d -> InvestmentRating.valueOf(d.getExternalId()),
                RatingSchemeItemRecord::getId);

    }


    private static long createRatingScheme(DSLContext waltz) {
        RatingSchemeRecord r = waltz.newRecord(rs);
        r.setName("Investment Status");
        r.setDescription("Scheme reflecting Invest/Disinvest/Maintain ratings");
        r.setExternalId("INVEST");

        r.store();

        LOG.debug("Created new scheme {}/{}", r.getExternalId(), r.getId());

        return r.getId();

    }


    private static long createCategory(DSLContext waltz,
                                       long rsId,
                                       Timestamp now) {
        MeasurableCategoryRecord r = waltz.newRecord(mc);
        r.setName("Capability");
        r.setDescription("Capability Taxonomy");
        r.setAllowPrimaryRatings(true);
        r.setExternalId("CAPABILITY");
        r.setIconName("puzzle-piece");
        r.setRatingSchemeId(rsId);
        r.setLastUpdatedAt(now);
        r.setLastUpdatedBy(USER_ID);
        r.store();

        LOG.debug("Created new category {}/{}", r.getExternalId(), r.getId());

        return r.getId();
    }


    private void blat(DSLContext waltz) {
        LOG.warn("Deleting lots of data");

        waltz.deleteFrom(alloc).execute();
        waltz.deleteFrom(allocScheme).execute();

        waltz.deleteFrom(mr).execute();
        waltz.deleteFrom(ar).execute();
        waltz.deleteFrom(m).execute();
        waltz.deleteFrom(mc).execute();
        waltz.deleteFrom(ad).execute();
        waltz.deleteFrom(rsi).execute();
        waltz.deleteFrom(rs).execute();

        waltz.deleteFrom(lf).execute();
        waltz.deleteFrom(lfd).execute();
        waltz.deleteFrom(dt).execute();

        waltz.deleteFrom(app).execute();
        waltz.deleteFrom(ou).execute();
    }


    public static void main(String[] args) throws Exception {
        LoggingUtilities.configureLogging();

        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DemoDataPopulator loader = ctx.getBean(DemoDataPopulator.class);

        loader.go();
    }


}
