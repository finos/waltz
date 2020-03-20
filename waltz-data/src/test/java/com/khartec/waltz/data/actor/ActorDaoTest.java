package com.khartec.waltz.data.actor;

import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.actor.ImmutableActorCreateCommand;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static org.junit.Assert.*;

public class ActorDaoTest {

    static ApplicationContext ctx;

    @BeforeClass
    public static void setUp() {
        LoggingUtilities.configureLogging();
        ctx = new AnnotationConfigApplicationContext(DITestingConfiguration.class);
    }


    @Test
    public void actorsCanBeCreated() {
        ActorDao dao = ctx.getBean(ActorDao.class);
        Long id = createActor(dao, "creationTest");

        Actor retrieved = dao.getById(id);
        assertEquals("creationTestName", retrieved.name());
        assertEquals("creationTestDesc", retrieved.description());
        assertTrue(retrieved.isExternal());
    }


    @Test
    public void actorsCanDeleteIfNotUsed() {
        ActorDao dao = ctx.getBean(ActorDao.class);
        int preCount = dao.findAll().size();
        Long id = createActor(dao, "canBeDeletedTest");
        boolean deleted = dao.deleteIfNotUsed(id);

        assertTrue("Actor should be deleted as not used in flows", deleted);
        assertEquals("After deletion the count of actors should be the same as before the actor was added", preCount, dao.findAll().size());
    }


    @Test
    public void actorsCannotDeleteIfUsed() {
        ActorDao dao = ctx.getBean(ActorDao.class);
        Long idA = createActor(dao, "cannotBeDeletedActorA");
        Long idB = createActor(dao, "cannotBeDeletedActorB");

        createFlow(
                mkRef(EntityKind.ACTOR, idA),
                mkRef(EntityKind.ACTOR, idB));

        int preCount = dao.findAll().size();
        boolean wasDeleted = dao.deleteIfNotUsed(idA);

        assertFalse("Actor should not be deleted as used in a flow", wasDeleted);
        assertEquals("After attempted deletion the count of actors should be the same", preCount, dao.findAll().size());

        System.out.println("--- " + dao.findAll().size());
    }


    // --- UTIL


    private void createFlow(EntityReference refA, EntityReference refB) {
        LogicalFlowDao flowDao = ctx.getBean(LogicalFlowDao.class);
        flowDao.addFlow(ImmutableLogicalFlow
                .builder()
                .source(refA)
                .target(refB)
                .lastUpdatedBy("admin")
                .build());
    }


    private Long createActor(ActorDao dao, String stem) {
        return dao.create(
                ImmutableActorCreateCommand
                        .builder()
                        .name(stem+"Name")
                        .description(stem+"Desc")
                        .isExternal(true)
                        .build(),
                "admin");
    }


}
