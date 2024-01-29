package org.finos.waltz.jobs.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.schema.tables.RatingScheme;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.schema.tables.records.ApplicationRecord;
import org.finos.waltz.schema.tables.records.DataTypeRecord;
import org.finos.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.finos.waltz.schema.tables.records.LogicalFlowRecord;
import org.finos.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.finos.waltz.schema.tables.records.MeasurableRecord;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.common.StringUtilities.isDefined;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class DemoDataLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataLoader.class);

    private static final OrganisationalUnit ou = Tables.ORGANISATIONAL_UNIT;
    private static final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY;
    private static final RatingScheme rs = Tables.RATING_SCHEME;
    private static final RatingSchemeItem rsi = Tables.RATING_SCHEME_ITEM;
    private static final Measurable m = Tables.MEASURABLE;
    private static final MeasurableRating mr = Tables.MEASURABLE_RATING;
    private static final Application app = Tables.APPLICATION;
    private static final DataType dt = Tables.DATA_TYPE;
    private static final LogicalFlow lf = Tables.LOGICAL_FLOW;
    private static final LogicalFlowDecorator lfd = Tables.LOGICAL_FLOW_DECORATOR;

    public static final String PROVENANCE = "test";
    public static final String USER_ID = "admin";

    private final DSLContext dsl;
    private final EntityHierarchyService hierarchyService;


    public DemoDataLoader(DSLContext dsl, EntityHierarchyService hierarchyService) {
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
            processOrgUnits(waltz, workbook.getSheet("org"), now);
            processCapabilities(waltz, workbook.getSheet("capabilities"), categoryAndScheme.v1, now);
            processApplications(waltz, workbook.getSheet("apps"), now);
            processAppToCapabilityMappings(waltz, workbook.getSheet("app-cap"), now);
            processDataTypes(waltz, workbook.getSheet("data-types"), now);
            processDataFlows(waltz, workbook.getSheet("flows"), now);

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


    private void processDataFlows(DSLContext waltz,
                                  Sheet flowSheet,
                                  Timestamp now) {

        Map<String, Long> appToIdMap = fetchAppExtIdToIdMap(waltz);
        Map<String, Long> dtToIdMap = fetchDataTypeExtIdToIdMap(waltz);
        Siphon<Tuple6<String, String, String, Long, Long, Long>> badRowSiphon = mkSiphon(t -> t.v4 == null || t.v5 == null || t.v6 == null);

        Set<Tuple3<Long, Long, Long>> resolvedData = StreamSupport
                .stream(flowSheet.spliterator(), false)
                .skip(1)
                .map(row -> tuple(strVal(row, Columns.A), strVal(row, Columns.B), strVal(row, Columns.C)))
                .map(t -> t.concat(tuple(appToIdMap.get(t.v1), appToIdMap.get(t.v2), dtToIdMap.get(t.v3))))
                .filter(badRowSiphon)
                .map(Tuple6::skip3)
                .collect(toSet());

        int lfInsertCount = summarizeResults(resolvedData
                 .stream()
                 .map(Tuple3::limit2)
                 .distinct()
                 .map(t -> {
                     LogicalFlowRecord r = waltz.newRecord(lf);
                     r.setSourceEntityId(t.v1);
                     r.setTargetEntityId(t.v2);
                     r.setSourceEntityKind(EntityKind.APPLICATION.name());
                     r.setTargetEntityKind(EntityKind.APPLICATION.name());
                     r.setProvenance(PROVENANCE);
                     r.setLastUpdatedAt(now);
                     r.setCreatedAt(now);
                     r.setLastUpdatedBy(USER_ID);
                     r.setCreatedBy(USER_ID);
                     return r;
                 })
                 .collect(collectingAndThen(toSet(), waltz::batchInsert))
                 .execute());

        Map<Tuple2<Long, Long>, Long> lfBySrcTarget = waltz
                .select(lf.SOURCE_ENTITY_ID, lf.TARGET_ENTITY_ID, lf.ID)
                .from(lf)
                .fetchMap(
                    r -> tuple(
                        r.get(lf.SOURCE_ENTITY_ID),
                        r.get(lf.TARGET_ENTITY_ID)),
                    r -> r.get(lf.ID));

        int lfdInsertCount = summarizeResults(resolvedData
                .stream()
                .map(t -> tuple(lfBySrcTarget.get(t.limit2()), t.v3)) // lfId, dtId
                .map(t -> {
                    LogicalFlowDecoratorRecord r = waltz.newRecord(lfd);
                    r.setLogicalFlowId(t.v1);
                    r.setDecoratorEntityId(t.v2);
                    r.setDecoratorEntityKind(EntityKind.DATA_TYPE.name());
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(USER_ID);
                    r.setProvenance(PROVENANCE);
                    return r;
                })
                .collect(collectingAndThen(toSet(), waltz::batchInsert))
                .execute());

        LOG.debug(
                "Created {} logical flows and {} decorations",
                lfInsertCount,
                lfdInsertCount);
    }


    private void processDataTypes(DSLContext waltz,
                                  Sheet dtSheet,
                                  Timestamp now) {

        AtomicLong idCtr = new AtomicLong(1L);
        Set<Tuple2<DataTypeRecord, String>> recordsAndParentExtIds = StreamSupport
                .stream(dtSheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    DataTypeRecord r = waltz.newRecord(dt);
                    r.setId(idCtr.getAndIncrement());
                    r.setCode(strVal(row, Columns.A));
                    r.setName(strVal(row, Columns.C));
                    r.setDescription(strVal(row, Columns.D));
                    r.setLastUpdatedAt(now);
                    return tuple(r, strVal(row, Columns.B));
                })
                .collect(toSet());

        int insertCount = summarizeResults(waltz
               .batchInsert(SetUtilities.map(recordsAndParentExtIds, t -> t.v1))
               .execute());

        Map<String, Long> dtToIdMap = fetchDataTypeExtIdToIdMap(waltz);

        int updCount = summarizeResults(recordsAndParentExtIds
                 .stream()
                 .filter(t -> isDefined(t.v2))
                 .map(t -> tuple(t.v1.getId(), dtToIdMap.get(t.v2)))
                 .filter(t -> t.v2 != null)
                 .map(t -> waltz.update(dt).set(dt.PARENT_ID, t.v2).where(dt.ID.eq(t.v1)))
                 .collect(collectingAndThen(toSet(), waltz::batch))
                 .execute());

        LOG.debug(
                "Created {} new data type records and updated {} parent ids",
                insertCount,
                updCount);
    }


    private void processAppToCapabilityMappings(DSLContext waltz,
                                                Sheet appToCapSheet,
                                                Timestamp now) {

        Map<String, Long> appToIdMap = fetchAppExtIdToIdMap(waltz);
        Map<String, Long> capToIdMap = fetchCapabilityExtIdToIdMap(waltz);

        Siphon<Tuple4<String, String, Long, Long>> badRowSiphon = mkSiphon(t -> t.v3 == null || t.v4 == null);

        int insertCount = summarizeResults(StreamSupport
             .stream(appToCapSheet.spliterator(), false)
             .skip(1)
             .map(row -> tuple(
                     strVal(row, Columns.A),
                     strVal(row, Columns.B)))
             .map(t -> tuple(
                     t.v1,
                     t.v2,
                     appToIdMap.get(t.v1),
                     capToIdMap.get(t.v2)))
             .filter(badRowSiphon)
             .map(t -> {
                 InvestmentRating rating = randomPick(
                         InvestmentRating.INVEST,
                         InvestmentRating.INVEST,
                         InvestmentRating.MAINTAIN,
                         InvestmentRating.MAINTAIN,
                         InvestmentRating.MAINTAIN,
                         InvestmentRating.DISINVEST);

                 MeasurableRatingRecord r = waltz.newRecord(mr);
                 r.setRating(rating.code);
                 r.setEntityKind(EntityKind.APPLICATION.name());
                 r.setEntityId(t.v3);
                 r.setMeasurableId(t.v4);
                 r.setProvenance(PROVENANCE);
                 r.setLastUpdatedAt(now);
                 r.setLastUpdatedBy(USER_ID);
                 return r;
             })
             .collect(collectingAndThen(toSet(), waltz::batchInsert))
             .execute());

        LOG.debug("Created {} app to capability mappings", insertCount);

        if (badRowSiphon.hasResults()) {
            LOG.info("Bad rows: {}", badRowSiphon.getResults());
        }
    }


    private void processApplications(DSLContext waltz, Sheet apps, Timestamp now) {
        Map<String, Long> ouExtIdToIdMap = waltz
            .select(ou.EXTERNAL_ID, ou.ID)
            .from(ou)
            .fetchMap(ou.EXTERNAL_ID, ou.ID);

        int insertCount = summarizeResults(StreamSupport
             .stream(apps.spliterator(), false)
             .skip(1)
             .map(row -> {
                 ApplicationRecord r = waltz.newRecord(app);
                 r.setName(strVal(row, Columns.B));
                 r.setDescription(strVal(row, Columns.D));
                 r.setAssetCode(strVal(row, Columns.A));
                 r.setOrganisationalUnitId(ouExtIdToIdMap.getOrDefault(strVal(row, Columns.C), 0L));
                 r.setOverallRating("A");
                 r.setProvenance(PROVENANCE);
                 r.setUpdatedAt(now);
                 r.setCreatedAt(now);
                 r.setBusinessCriticality("A");

                 return r;
             })
             .collect(collectingAndThen(
                     toSet(),
                     waltz::batchStore))
             .execute());

        LOG.debug("Created {} new applications", insertCount);
    }


    private void processCapabilities(DSLContext waltz,
                                     Sheet capSheet,
                                     Long mcId,
                                     Timestamp now) {

        Set<MeasurableRecord> records = StreamSupport
            .stream(capSheet.spliterator(), false)
            .skip(1)
            .map(row -> {
                MeasurableRecord r = waltz.newRecord(m);
                r.setName(strVal(row, Columns.B));
                r.setDescription(strVal(row, Columns.D));
                r.setExternalId(strVal(row, Columns.A));
                r.setExternalParentId(strVal(row, Columns.C));
                r.setProvenance(PROVENANCE);
                r.setLastUpdatedAt(now);
                r.setLastUpdatedBy(USER_ID);
                r.setMeasurableCategoryId(mcId);
                return r;
            })
            .collect(toSet());

        int insertCount = summarizeResults(waltz.batchStore(records).execute());

        Map<String, MeasurableRecord> extToRecordMap = waltz.selectFrom(m).fetchMap(m.EXTERNAL_ID);

        int updateCount = summarizeResults(
            extToRecordMap
                 .values()
                 .stream()
                 .filter(r -> isDefined(r.getExternalParentId()))
                 .map(r -> waltz
                         .update(m)
                         .set(m.PARENT_ID, extToRecordMap.get(r.getExternalParentId()).getId())
                         .where(m.ID.eq(r.getId())))
                 .collect(collectingAndThen(toSet(), waltz::batch))
                 .execute());

        LOG.debug(
            "Created {} measurables in category {}, then updated {} parentIds",
            insertCount,
            mcId,
            updateCount);
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


    private void processOrgUnits(DSLContext waltz,
                                 Sheet orgSheet,
                                 Timestamp now) {
        AtomicLong idProvider = new AtomicLong(1);
        Set<Tuple2<OrganisationalUnitRecord, String>> recordsWithParentIds = StreamSupport
                .stream(orgSheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    OrganisationalUnitRecord r = waltz.newRecord(ou);
                    r.setId(idProvider.getAndIncrement());
                    r.setName(strVal(row, Columns.B));
                    r.setDescription(strVal(row, Columns.D));
                    r.setExternalId(strVal(row, Columns.A));
                    r.setProvenance("Waltz");
                    r.setLastUpdatedAt(now);
                    r.setCreatedAt(now);
                    r.setCreatedBy(USER_ID);
                    r.setLastUpdatedBy(USER_ID);
                    return tuple(r, strVal(row, Columns.C));
                })
                .collect(Collectors.toSet());

        int insertCount = summarizeResults(
            waltz
                 .batchInsert(SetUtilities.map(
                         recordsWithParentIds,
                         t -> t.v1))
                 .execute());

        Map<String, OrganisationalUnitRecord> extIdToOuMap = waltz
                .selectFrom(ou)
                .fetchMap(ou.EXTERNAL_ID);

        int updateCount = summarizeResults(
                recordsWithParentIds
                     .stream()
                     .filter(t -> isDefined(t.v2))
                     .map(t -> t.concat(extIdToOuMap.get(t.v2)))
                     .map(t -> waltz
                         .update(ou)
                         .set(ou.PARENT_ID, t.v3.getId())
                        .where(ou.EXTERNAL_ID.eq(t.v1.getExternalId())))
                     .collect(collectingAndThen(Collectors.toSet(), waltz::batch))
                     .execute());

        LOG.debug("Created {} org units and updated {} parent ids", insertCount, updateCount);
    }


    private static Map<String, Long> fetchCapabilityExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(m.EXTERNAL_ID, m.ID)
                .from(m)
                .fetchMap(m.EXTERNAL_ID, m.ID);
    }


    private static Map<String, Long> fetchAppExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(app.ASSET_CODE, app.ID)
                .from(app)
                .fetchMap(app.ASSET_CODE, app.ID);
    }


    private static Map<String, Long> fetchDataTypeExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(dt.CODE, dt.ID)
                .from(dt)
                .fetchMap(dt.CODE, dt.ID);
    }


    private void blat(DSLContext waltz) {
        LOG.warn("Deleting lots of data");
        waltz.deleteFrom(mr).execute();
        waltz.deleteFrom(app).execute();
        waltz.deleteFrom(ou).execute();
        waltz.deleteFrom(m).execute();
        waltz.deleteFrom(mc).execute();
        waltz.deleteFrom(rsi).execute();
        waltz.deleteFrom(rs).execute();
        waltz.deleteFrom(lf).execute();
        waltz.deleteFrom(lfd).execute();
        waltz.deleteFrom(dt).execute();
    }


    private static String strVal(Row row,
                                 int col) {
        Cell cell = row.getCell(col);
        return cell == null ? null : cell.getStringCellValue();
    }


    public static void main(String[] args) throws Exception {
        LoggingUtilities.configureLogging();

        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DemoDataLoader loader = ctx.getBean(DemoDataLoader.class);

        loader.go();
    }


}
