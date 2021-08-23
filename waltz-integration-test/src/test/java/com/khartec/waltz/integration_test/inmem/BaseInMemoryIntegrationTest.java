package com.khartec.waltz.integration_test.inmem;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.ImmutableActorCreateCommand;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupKind;
import com.khartec.waltz.model.app_group.ImmutableAppGroup;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableAppRegistrationRequest;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.schema.tables.records.RatingSchemeRecord;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.h2.tools.Server;
import org.jooq.DSLContext;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;

public class BaseInMemoryIntegrationTest {

    static {
        LoggingUtilities.configureLogging();
    }


    public static class OuIds {
        public Long root;
        public Long a;
        public Long a1;
        public Long b;
    }


    public static class Daos {
        public LogicalFlowDao logicalFlowDao;
        public OrganisationalUnitDao organisationalUnitDao;
    }


    public static class Helpers {
        public LogicalFlowHelper logicalFlowHelper;
    }


    protected static final AtomicLong counter = new AtomicLong(1_000_000);

    protected static AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIInMemoryTestConfiguration.class);

    protected static final OrganisationalUnitIdSelectorFactory ouSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    protected static final ApplicationIdSelectorFactory appSelectorFactory = new ApplicationIdSelectorFactory();
    protected static final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    protected static final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
;

    protected static final String LAST_UPDATE_USER = "last";
    protected static final String PROVENANCE = "test";



    protected OuIds ouIds;
    protected Daos daos;
    protected Helpers helpers;

    /**
     *   - root
     *   -- a
     *   --- a1
     *   -- b
     */
    private OuIds setupOuTree() {
        getDsl().deleteFrom(ORGANISATIONAL_UNIT).execute();
        OuIds ids = new OuIds();
        ids.root = createOrgUnit("root", null);
        ids.a = createOrgUnit("a", ids.root);
        ids.a1 = createOrgUnit("a1", ids.a);
        ids.b = createOrgUnit("b", ids.root);

        rebuildHierarchy(EntityKind.ORG_UNIT);
        return ids;
    }

    @Before
    public void baseSetup() {
        ouIds = setupOuTree();
        daos = setupDaos();
        helpers = setupHelpers();
    }


    private Helpers setupHelpers() {
        Helpers h = new Helpers();
        h.logicalFlowHelper = new LogicalFlowHelper(daos.logicalFlowDao);
        return h;
    }


    private Daos setupDaos() {
        Daos d = new Daos();
        d.logicalFlowDao = ctx.getBean(LogicalFlowDao.class);
        d.organisationalUnitDao = ctx.getBean(OrganisationalUnitDao.class);
        return d;
    }


    public DSLContext getDsl() {
        return ctx.getBean(DSLContext.class);
    }


    public EntityReference mkNewAppRef() {
        return mkRef(
                EntityKind.APPLICATION,
                counter.incrementAndGet());
    }


    protected void rebuildHierarchy(EntityKind kind) {
        EntityHierarchyService ehSvc = ctx.getBean(EntityHierarchyService.class);
        ehSvc.buildFor(kind);
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
        record.setId(counter.incrementAndGet());
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



    public Long createAppGroupWithAppRefs(String groupName, Collection<EntityReference> appRefs) {
        Collection<Long> appIds = CollectionUtilities.map(appRefs, EntityReference::id);
        return createAppGroupWithAppIds(groupName, appIds);
    }


    public Long createAppGroupWithAppIds(String groupName, Collection<Long> appIds) {
        AppGroup g = ImmutableAppGroup
                .builder()
                .name(groupName)
                .appGroupKind(AppGroupKind.PUBLIC)
                .build();

        Long gId = ctx.getBean(AppGroupDao.class)
                .insert(g);

        ctx.getBean(AppGroupEntryDao.class)
                .addApplications(gId, appIds);

        return gId;
    }


    /**
     * DEBUG ONLY
     *
     * Uncomment the @After annotation to get the test executor
     * to pause for 2 hours.  During this time you can
     * attach an external database client to the in memory H2
     * instance to see the current state of the database.
     *
     * The url to connect to is:
     *    jdbc:h2:tcp://localhost/mem:waltz
     */
//    @After
    public void stickAround() {
        try {
            System.err.println("Starting tcp server, connect with: jdbc:h2:tcp://localhost/mem:waltz");
            Server.createTcpServer().start();
            System.err.println("Sticking around for 2 hrs");
            Thread.sleep(TimeUnit.HOURS.toMillis(2));
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }
}
