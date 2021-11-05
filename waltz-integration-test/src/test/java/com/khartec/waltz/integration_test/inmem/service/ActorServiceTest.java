/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.integration_test.inmem.service;

import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.integration_test.inmem.helpers.ActorHelper;
import com.khartec.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.actor.Actor;
import com.khartec.waltz.service.actor.ActorService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.junit.Assert.*;


public class ActorServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private ActorHelper helper;

    @Autowired
    private ActorService svc;

    @Autowired
    private LogicalFlowHelper logicalFlowHelper;

    @Test
    public void actorsCanBeCreated() {
        String name = mkName("actorsCanBeCreated");
        Long id = helper.createActor(name);

        Actor retrieved = svc.getById(id);
        assertEquals(name, retrieved.name());
        assertEquals(name + " Desc", retrieved.description());
        assertTrue(retrieved.isExternal());
    }


    @Test
    public void actorsCanBeDeletedIfNotUsed() {
        int preCount = svc.findAll().size();
        Long id = helper.createActor(mkName("canBeDeletedTest"));

        System.out.println("After creation: "+ svc.findAll());
        boolean deleted = svc.delete(id);

        assertTrue("Actor should be deleted as not used in flows", deleted);
        assertEquals("After deletion the count of actors should be the same as before the actor was added", preCount, svc.findAll().size());
    }


    @Test
    public void actorsCannotBeDeletedIfUsed() {
        Long idA = helper.createActor(mkName("cannotBeDeletedActorA"));
        Long idB = helper.createActor(mkName("cannotBeDeletedActorB"));

        logicalFlowHelper.createLogicalFlow(
                mkRef(EntityKind.ACTOR, idA),
                mkRef(EntityKind.ACTOR, idB));

        int preCount = svc.findAll().size();
        boolean wasDeleted = svc.delete(idA);

        assertFalse("Actor should not be deleted as used in a flow", wasDeleted);
        assertEquals("After attempted deletion the count of actors should be the same", preCount, svc.findAll().size());
    }


    @Test
    public void actorsCanBeSearched() {
        List<EntityReference> noHits = svc.search("wibble");
        assertTrue(noHits.isEmpty());

        String a = mkName("searchActorA");
        String b = mkName("searchActorB");

        Long aId = helper.createActor(a);
        helper.createActor(b);

        List<EntityReference> hits = svc.search(a + " " + "desc");
        assertEquals(1, hits.size());
        assertEquals(first(hits), mkRef(EntityKind.ACTOR, aId));
    }

}