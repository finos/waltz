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
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.HierarchyQueryScope.CHILDREN;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.Collections.emptyList;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class LogicalFlowServiceTest extends BaseInMemoryIntegrationTest {

    private LogicalFlowService lfSvc;
    private LogicalFlowHelper helper;


    @Before
    public void setupOuTest() {
        lfSvc = services.logicalFlowService;
        helper = helpers.logicalFlowHelper;
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
        EntityReference c = createNewApp("c", ouIds.b);
        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, b);

        assertThrows("Expect no flows to be removed where id cannot be found",
                IllegalArgumentException.class,
                () -> lfSvc.removeFlow(-1L, "logicalFlowServiceTestRemoveFlow"));

        int removedForExistingIdCount = lfSvc.removeFlow(ab.id().get(), "logicalFlowServiceTestRemoveFlow");
        assertEquals("Expect only one flow to be removed", 1, removedForExistingIdCount);

    }


    @Test
    public void findActiveByFlowIdsTest(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);
        EntityReference d = createNewApp("d", ouIds.b);

        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);
        LogicalFlow ad = helper.createLogicalFlow(a, d);

        int removedFlowCount = lfSvc.removeFlow(ab.id().get(), "logicalFlowServiceTestRemoveFlow");

        Collection<LogicalFlow> activeFlows = lfSvc
                .findActiveByFlowIds(asSet(ab.id().get(), ac.id().get(), ad.id().get()));

        Set<Long> activeFlowIds = map(activeFlows, r -> r.id().get());

        assertEquals(activeFlows.size(), 2);
        assertEquals("", asSet(ad.id().get(), ac.id().get()), activeFlowIds);
        assertEquals("", asSet(ac.id().get(), ad.id().get()), activeFlowIds);
    }


    @Test
    public void findBySourceAndTargetEntityReferences(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);

        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);

        Tuple2<EntityReference, EntityReference> abSrcTarget = tuple(a, b);
        Tuple2<EntityReference, EntityReference> acSrcTarget = tuple(a, c);
        Tuple2<EntityReference, EntityReference> bcSrcTarget = tuple(b, c);

        List<LogicalFlow> flowsFromEmptySearch = lfSvc.findBySourceAndTargetEntityReferences(emptyList());
        assertEquals(emptyList(), flowsFromEmptySearch);

        List<LogicalFlow> abFlowSearch = lfSvc.findBySourceAndTargetEntityReferences(asList(abSrcTarget));
        assertEquals(1, abFlowSearch.size());

        List<LogicalFlow> abacFlowSearch = lfSvc.findBySourceAndTargetEntityReferences(asList(abSrcTarget, acSrcTarget));
        assertEquals(2, abacFlowSearch.size());

        List<LogicalFlow> flowSearchWhereSrcTrgNotFound = lfSvc.findBySourceAndTargetEntityReferences(asList(abSrcTarget, acSrcTarget, bcSrcTarget));
        assertEquals(2 , flowSearchWhereSrcTrgNotFound.size());

    }


    @Test
    public void addFlow(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        createUnknownDatatype();

        ImmutableAddLogicalFlowCommand createLoopCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(a)
                .build();

        ImmutableAddLogicalFlowCommand createCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(b)
                .build();

        assertThrows(
                "If source and target are the same, flow rejected and exception thrown",
                IllegalArgumentException.class,
                () -> lfSvc.addFlow(createLoopCommand, "addFlowTest"));


        LogicalFlow newFlow = lfSvc.addFlow(createCommand, "addFlowTest");
        assertEquals("Flow created should have the same source as in the create command", a, newFlow.source());
        assertEquals("Flow created should have the same target as in the create command", b, newFlow.target());
        assertEquals("Flow created should have the user as the last updated by value", "addFlowTest", newFlow.lastUpdatedBy());

    }


    @Test
    public void addFlows(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);

        createUnknownDatatype();

        ImmutableAddLogicalFlowCommand createLoopCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(a)
                .build();

        ImmutableAddLogicalFlowCommand abCreateCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(b)
                .build();

        ImmutableAddLogicalFlowCommand baCreateCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(b)
                .target(a)
                .build();

        ImmutableAddLogicalFlowCommand bcCreateCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(b)
                .target(c)
                .build();

        List<LogicalFlow> noCreateCommands = lfSvc.addFlows(emptyList(), "addFlowTest");
        assertEquals("If no list provided returns empty list", emptyList(), noCreateCommands);

        assertThrows(
                "If contains invalid flow (same src and trg) throws exception",
                IllegalArgumentException.class,
                () -> lfSvc.addFlows(asList(createLoopCommand, abCreateCommand), "addFlowTest"));

        List<LogicalFlow> newFlows = lfSvc.addFlows(asList(abCreateCommand, baCreateCommand), "addFlowsTest");
        assertEquals("2 valid create commands should create 2 flows", 2, newFlows.size());

        assertEquals("Source and targets in returned set match",
                asSet(tuple(a, b), tuple(b, a)),
                map(newFlows, f -> tuple(f.source(), f.target())));

        List<LogicalFlow> duplicatedFlows = lfSvc.addFlows(asList(bcCreateCommand, bcCreateCommand), "addFlowsTest");
        assertEquals("multiple create commands for same source and target should not create multiple flows",
                1,
                duplicatedFlows.size());

        assertEquals("multiple create commands for same source and target should not create multiple flows",
                asSet(tuple(b, c)),
                map(duplicatedFlows, f -> tuple(f.source(), f.target())));

        List<LogicalFlow> existingFlows = lfSvc.addFlows(asList(bcCreateCommand, abCreateCommand), "addFlowsTest");
        assertTrue("should not create flow if flow already exists", existingFlows.isEmpty());
    }


    @Test
    public void restoreFlow(){

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);

        LogicalFlow ab = helper.createLogicalFlow(a, b);

        lfSvc.removeFlow(ab.id().get(), "restoreFlowTest");

        boolean restoreInvalid = lfSvc.restoreFlow(-1, "restoreFlowTest");

        assertFalse("Cannot restore a flow that doesn't exist", restoreInvalid);

        boolean restoredFlow = lfSvc.restoreFlow(ab.id().get(), "restoreFlowTest");
        assertTrue("Restores flow if exists", restoredFlow);

        LogicalFlow flow = lfSvc.getById(ab.id().get());

        assertNotEquals(
                "Restored flow should not have a lifecycle status of 'REMOVED'",
                EntityLifecycleStatus.REMOVED.name(),
                flow.entityLifecycleStatus().name());

        assertFalse("Restored flow should not be removed", flow.isRemoved());

        assertEquals("Restored flow should have last updated by user", "restoreFlowTest", flow.lastUpdatedBy());
    }


    @Test
    public void cleanupOrphans(){

        clearAllFlows();

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);

        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);
        LogicalFlow ca = helper.createLogicalFlow(c, a);

        int flowsRemoved = lfSvc.cleanupOrphans();

        assertEquals("No flows removed if all apps are active", 0, flowsRemoved);

        removeApp(c.id());

        int flowsRemovedAfterAppRemoved = lfSvc.cleanupOrphans();

        assertEquals("Flows removed where either source or target is retired",
                2,
                flowsRemovedAfterAppRemoved);

        LogicalFlow flowWhereTargetRemoved = lfSvc.getById(ac.id().get());

        assertEquals("If target removed, flow still exists but has entity lifecycle status of 'REMOVED'",
                EntityLifecycleStatus.REMOVED,
                flowWhereTargetRemoved.entityLifecycleStatus());
    }


    @Test
    public void cleanupSelfReferencingFlows() {

        clearAllFlows();

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);

        int removedFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals("Nothing removed if no logical flows", 0, removedFlows);

        LogicalFlow ab = helper.createLogicalFlow(a, b);

        int removedWhereNoSelfReferencingFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals("Nothing removed if no self-referencing logical flows", 0, removedWhereNoSelfReferencingFlows);

        LogicalFlow aa = helper.createLogicalFlow(a, a);
        LogicalFlow bb = helper.createLogicalFlow(b, b);

        int removedAllWhereSelfReferencingFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals("Removed all self-referencing logical flows", 2, removedAllWhereSelfReferencingFlows);

        LogicalFlow aaFlow = lfSvc.getById(aa.id().get());
        assertEquals("Self referencing flow still exists but is removed", EntityLifecycleStatus.REMOVED, aaFlow.entityLifecycleStatus());
    }


    @Test
    public void findUpstreamFlowsForEntityReferences() {

        clearAllFlows();

        EntityReference a = createNewApp("a", ouIds.a);
        EntityReference b = createNewApp("b", ouIds.a1);
        EntityReference c = createNewApp("c", ouIds.b);
        EntityReference d = createNewApp("d", ouIds.b);

        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow cb = helper.createLogicalFlow(c, b);
        LogicalFlow bc = helper.createLogicalFlow(b, c);
        LogicalFlow bd = helper.createLogicalFlow(b, d);

        Collection<LogicalFlow> upstreamFlowsForEmptyList = lfSvc.findUpstreamFlowsForEntityReferences(emptyList());

        assertTrue("No upstreams when no ref provided", isEmpty(upstreamFlowsForEmptyList));

        Collection<LogicalFlow> upstreamsWhereOnlySource = lfSvc.findUpstreamFlowsForEntityReferences(asList(a));
        assertEquals("No upstreams when all refs are only targets", 0, upstreamsWhereOnlySource.size());

        Collection<LogicalFlow> allUpstreams = lfSvc.findUpstreamFlowsForEntityReferences(asList(b, c));
        assertEquals("Returns all upstreams but not downstreams", 3, allUpstreams.size());
    }

}