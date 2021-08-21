package com.khartec.waltz.integration_test.inmem;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
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
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;

public class InMemoryPoCTest {

    protected static AnnotationConfigApplicationContext ctx;

    private static final AtomicLong ctr = new AtomicLong(1_000_000);
    protected OuIds ouIds;

    protected static class OuIds {
        Long root;
        Long a;
        Long a1;
        Long b;
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
