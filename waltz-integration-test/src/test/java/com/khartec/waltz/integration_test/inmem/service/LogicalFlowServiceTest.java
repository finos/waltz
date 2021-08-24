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
import com.khartec.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.junit.Before;
import org.junit.Test;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.HierarchyQueryScope.CHILDREN;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.Assert.*;

public class LogicalFlowServiceTest extends BaseInMemoryIntegrationTest {

    private LogicalFlowService lfSvc;
    private LogicalFlowHelper helper;


    @Before
    public void setupOuTest() {
        lfSvc = services.logicalFlowService;
        helper = helpers.logicalFlowHelper;
        System.out.println("ouTest::setup");
    }


    @Test
    public void basicDirectAssociations() {
        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);
        EntityReference d = createNewApp("c", ouIds.b);
        // a -> b
        // a -> d
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ad = helper.createLogicalFlow(a, d);

        assertEquals(
                "Can see flow associated to 'a'",
                asSet(ab.id(), ad.id()),
                map(lfSvc.findByEntityReference(a), IdProvider::id));

        assertEquals(
                "Can sees flows associated to 'b'",
                asSet(ab.id()),
                map(lfSvc.findByEntityReference(b), IdProvider::id));

        assertEquals(
                "Can sees flows associated to 'd'",
                asSet(ad.id()),
                map(lfSvc.findByEntityReference(d), IdProvider::id));

        assertTrue(
                "Can sees nothing associated to 'c'",
                isEmpty(lfSvc.findByEntityReference(c)));
    }


    @Test
    public void bySelector() {

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);
        // a -> b
        // a -> c
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);

        assertEquals("find by root ou gives all",
                asSet(ab.id(), ac.id()),
                map(lfSvc.findBySelector(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.root),
                        CHILDREN)),
                    IdProvider::id));

        assertEquals("find by ou 'b' gives only one flow",
                asSet(ac.id()),
                map(lfSvc.findBySelector(mkOpts(
                        mkRef(EntityKind.ORG_UNIT, ouIds.b),
                        CHILDREN)),
                    IdProvider::id));
    }


    @Test
    public void getByIdTest(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);

        LogicalFlow logFlow = lfSvc.getById(ab.id().get());

        assertEquals("Retrieved flow has correct source", ab.source(), logFlow.source());
        assertEquals("Retrieved flow has correct target", ab.target(), logFlow.target());

        LogicalFlow impossibleIdFlow = lfSvc.getById(-1L);

        assertNull("Returns null if id not found", impossibleIdFlow);
    }


    @Test
    public void removeFlowTest(){
        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);

        int removedForNoIdCount = lfSvc.removeFlow(-1L, "logicalFlowServiceTestRemoveFlow");

        assertEquals("Expect no flows to be removed where id cannot be found", removedForNoIdCount, 0);

//        int removedForExistingIdCount = lfSvc.removeFlow(ab.id().get(), "logicalFlowServiceTestRemoveFlow");

//        assertEquals("Expect only one flow to be removed", removedForExistingIdCount, 1);


    }

//
//    @Test
//    public void findActiveByFlowIdsTest(){
//
//        EntityReference a = createNewApp("a", ouIds.a);
//        EntityReference b = createNewApp("b", ouIds.a1);
//        EntityReference c = createNewApp("c", ouIds.b);
//        EntityReference d = createNewApp("d", ouIds.b);
//
//        // a -> b
//        // a -> c
//        LogicalFlow ab = helper.createLogicalFlow(a, b);
//        LogicalFlow ac = helper.createLogicalFlow(a, c);
//        LogicalFlow ad = helper.createLogicalFlow(a, d);
//
//        LogicalFlow logFlow = lfSvc.getById(ab.id().get());
//
//        assertEquals("Retrieved flow has correct source", ab.source(), logFlow.source());
//        assertEquals("Retrieved flow has correct target", ab.target(), logFlow.target());
//
//        LogicalFlow impossibleIdFlow = lfSvc.getById(-1L);
//
//        assertNull("Returns null if id not found", impossibleIdFlow);
//    }

}