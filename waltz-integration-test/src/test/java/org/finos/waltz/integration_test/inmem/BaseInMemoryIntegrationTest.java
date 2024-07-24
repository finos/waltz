package org.finos.waltz.integration_test.inmem;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.h2.tools.Server;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DIInMemoryTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class BaseInMemoryIntegrationTest {

    static {
        LoggingUtilities.configureLogging();
    }


    public static class OuIds {
        public Long root;
        public Long a;
        public Long a1;
        public Long b;
    }

    protected static final AtomicLong counter = new AtomicLong(1_000_000);

    protected static AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIInMemoryTestConfiguration.class);

    protected static final OrganisationalUnitIdSelectorFactory ouSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    protected static final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    protected static final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();

    protected static final String LAST_UPDATE_USER = "last";
    protected static final String PROVENANCE = "test";

    protected OuIds ouIds;

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

    @BeforeEach
    public void baseSetup() {
        LoggingUtilities.configureLogging();
        ouIds = setupOuTree();
    }


    private DSLContext getDsl() {
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


    /**
     * DEBUG ONLY
     * <p>
     * Uncomment the @AfterAll annotation to get the test executor
     * to pause for 2 hours.  During this time you can
     * attach an external database client to the in memory H2
     * instance to see the current state of the database.
     * <p>
     * The url to connect to is:
     * jdbc:h2:tcp://localhost/mem:waltz
     * username: sa
     * password: sa
     */
//    @AfterAll
    public static void stickAround() {
        try {
            System.err.println("Starting tcp server, connect with: jdbc:h2:tcp://localhost/mem:waltz, username: sa, password: sa");
            Server.createTcpServer().start();
            System.err.println("Sticking around for 2 hrs");
            Thread.sleep(TimeUnit.HOURS.toMillis(2));
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }

}
