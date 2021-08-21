package com.khartec.waltz.integration_test.inmem;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.ImmutableActorCreateCommand;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableAppRegistrationRequest;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.Tables.RATING_SCHEME;

public class BaseInMemoryIntegrationTest {

    protected static AnnotationConfigApplicationContext ctx;

    private static final AtomicLong ctr = new AtomicLong(1_000_000);
    protected OuIds ouIds;

    protected static final String LAST_UPDATE_USER = "last";
    protected static final String PROVENANCE = "test";


    public static class OuIds {
        public Long root;
        public Long a;
        public Long a1;
        public Long b;
    }


    private OuIds setupOuTree() {
        getDsl().deleteFrom(ORGANISATIONAL_UNIT).execute();
        OuIds ids = new OuIds();
        ids.root = createOrgUnit("root", null);
        ids.a = createOrgUnit("a", ids.root);
        ids.a1 = createOrgUnit("a1", ids.a);
        ids.b = createOrgUnit("b", ids.root);

        rebuildHierarachy(EntityKind.ORG_UNIT);
        return ids;
    }


    @BeforeClass
    public static void baseClassSetUp() {
        LoggingUtilities.configureLogging();
        ctx = new AnnotationConfigApplicationContext(InMemoryTestConfiguration.class);
    }


    @Before
    public void baseSetup() {
        System.out.println("base::setup");
        ouIds = setupOuTree();
    }


    public DSLContext getDsl() {
        return ctx.getBean(DSLContext.class);
    }


    public EntityReference mkNewAppRef() {
        return mkRef(
                EntityKind.APPLICATION,
                ctr.incrementAndGet());
    }


    protected void rebuildHierarachy(EntityKind kind) {
        EntityHierarchyService ehSvc = ctx.getBean(EntityHierarchyService.class);
        ehSvc.buildFor(kind);
    }


    public LogicalFlow createLogicalFlow(EntityReference refA, EntityReference refB) {
        LogicalFlowDao dao = ctx.getBean(LogicalFlowDao.class);
        return dao.addFlow(ImmutableLogicalFlow
                .builder()
                .source(refA)
                .target(refB)
                .lastUpdatedBy("admin")
                .build());
    }


    public Long createActor(String nameStem) {
        ActorDao dao = ctx.getBean(ActorDao.class);
        return dao.create(
                ImmutableActorCreateCommand
                        .builder()
                        .name(nameStem + "Name")
                        .description(nameStem + "Desc")
                        .isExternal(true)
                        .build(),
                "admin");
    }


    public Long createOrgUnit(String nameStem, Long parentId) {
        OrganisationalUnitRecord record = getDsl().newRecord(ORGANISATIONAL_UNIT);
        record.setId(ctr.incrementAndGet());
        record.setName(nameStem + "Name");
        record.setDescription(nameStem + "Desc");
        record.setParentId(parentId);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setProvenance("integration-test");
        record.insert();

        return record.getId();
    }

    public EntityReference createNewApp(String name, Long ouId) {
        AppRegistrationResponse resp = ctx.getBean(ApplicationDao.class)
                .registerApp(ImmutableAppRegistrationRequest.builder()
                        .name(name)
                        .organisationalUnitId(ouId != null ? ouId : 1L)
                        .applicationKind(ApplicationKind.IN_HOUSE)
                        .businessCriticality(Criticality.MEDIUM)
                        .lifecyclePhase(LifecyclePhase.PRODUCTION)
                        .overallRating(RagRating.G)
                        .businessCriticality(Criticality.MEDIUM)
                        .build());

        return resp.id().map(id -> mkRef(EntityKind.APPLICATION, id)).get();
    }


    protected long createMeasurableCategory(String name) {
        MeasurableCategoryDao dao = ctx.getBean(MeasurableCategoryDao.class);
        Set<MeasurableCategory> categories = dao.findByExternalId(name);
        return CollectionUtilities
                .maybeFirst(categories)
                .map(c -> c.id().get())
                .orElseGet(() -> {
                    long schemeId = createEmptyRatingScheme("test");
                    MeasurableCategoryRecord record = getDsl().newRecord(MEASURABLE_CATEGORY);
                    record.setDescription(name);
                    record.setName(name);
                    record.setExternalId(name);
                    record.setRatingSchemeId(schemeId);
                    record.setLastUpdatedBy("admin");
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.setEditable(false);
                    record.store();
                    return record.getId();
                });
    }


    private long createEmptyRatingScheme(String name) {
        DSLContext dsl = getDsl();
        return dsl
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .where(RATING_SCHEME.NAME.eq(name))
                .fetchOptional(RATING_SCHEME.ID)
                .orElseGet(() -> {
                    RatingSchemeRecord record = dsl.newRecord(RATING_SCHEME);
                    record.setName(name);
                    record.setDescription(name);
                    record.store();
                    return record.getId();
                });
    }


    protected long createMeasurable(String name, long categoryId) {
        return getDsl()
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.EXTERNAL_ID.eq(name))
                .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetchOptional(MEASURABLE.ID)
                .orElseGet(() -> {
                    MeasurableRecord record = getDsl().newRecord(MEASURABLE);
                    record.setMeasurableCategoryId(categoryId);
                    record.setName(name);
                    record.setDescription(name);
                    record.setConcrete(true);
                    record.setExternalId(name);
                    record.setProvenance(PROVENANCE);
                    record.setLastUpdatedBy(LAST_UPDATE_USER);
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.store();
                    return record.getId();
                });
    }
//
//    @Test
//    public void foo() {
//        System.out.println("Hello world");
//        assertTrue(1 == 1);
//        DSLContext dsl = ctx.getBean(DSLContext.class);
//        System.out.println(dsl.fetch("SHOW TABLES"));
//
//        dsl.selectFrom(LOGICAL_FLOW).fetch().format(System.out);
//    }
}
