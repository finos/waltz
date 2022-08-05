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

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.test_common_again.helpers.AppHelper;
import org.finos.waltz.test_common_again.helpers.DataTypeHelper;
import org.finos.waltz.test_common_again.helpers.LogicalFlowHelper;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;

public class LogicalFlowServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private LogicalFlowService lfSvc;

    @Autowired
    private LogicalFlowHelper helper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;


    @Test
    public void basicDirectAssociations() {
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        EntityReference d = appHelper.createNewApp("c", ouIds.b);
        // a -> b
        // a -> d
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ad = helper.createLogicalFlow(a, d);

        assertEquals(
                asSet(ab.id(), ad.id()),
                map(lfSvc.findByEntityReference(a), IdProvider::id),
                "Can see flow associated to 'a'");

        assertEquals(
                asSet(ab.id()),
                map(lfSvc.findByEntityReference(b), IdProvider::id),
                "Can sees flows associated to 'b'");

        assertEquals(
                asSet(ad.id()),
                map(lfSvc.findByEntityReference(d), IdProvider::id),
                "Can sees flows associated to 'd'");

        assertTrue(
                isEmpty(lfSvc.findByEntityReference(c)),
                "Can sees nothing associated to 'c'");
    }


    @Test
    public void bySelector() {

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        // a -> b
        // a -> c
        // c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);

        assertEquals(asSet(ab.id(), ac.id()),
                map(lfSvc.findBySelector(mkOpts(
                                mkRef(EntityKind.ORG_UNIT, ouIds.root),
                                CHILDREN)),
                        IdProvider::id),
                "find by root ou gives all");

        assertEquals(asSet(ac.id()),
                map(lfSvc.findBySelector(mkOpts(
                                mkRef(EntityKind.ORG_UNIT, ouIds.b),
                                CHILDREN)),
                        IdProvider::id),
                "find by ou 'b' gives only one flow");
    }


    @Test
    public void getByIdTest() {

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);

        LogicalFlow logFlow = lfSvc.getById(ab.id().get());

        assertEquals(ab.source(), logFlow.source(), "Retrieved flow has correct source");
        assertEquals(ab.target(), logFlow.target(), "Retrieved flow has correct target");

        LogicalFlow impossibleIdFlow = lfSvc.getById(-1L);

        assertNull(impossibleIdFlow, "Returns null if id not found");
    }


    @Test
    public void removeFlowTest() {
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        // a -> b
        // a -> c
        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, b);

        assertThrows(IllegalArgumentException.class,
                () -> lfSvc.removeFlow(-1L, "logicalFlowServiceTestRemoveFlow"),
                "Expect no flows to be removed where id cannot be found");

        int removedForExistingIdCount = lfSvc.removeFlow(ab.id().get(), "logicalFlowServiceTestRemoveFlow");
        assertEquals(1, removedForExistingIdCount, "Expect only one flow to be removed");

    }


    @Test
    public void findActiveByFlowIdsTest(){

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        EntityReference d = appHelper.createNewApp("d", ouIds.b);

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
        assertEquals(asSet(ad.id().get(), ac.id().get()), activeFlowIds);
        assertEquals(asSet(ac.id().get(), ad.id().get()), activeFlowIds);
    }


    @Test
    public void findBySourceAndTargetEntityReferences(){

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

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

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        dataTypeHelper.createUnknownDatatype();

        ImmutableAddLogicalFlowCommand createLoopCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(a)
                .build();

        ImmutableAddLogicalFlowCommand createCommand = ImmutableAddLogicalFlowCommand.builder()
                .source(a)
                .target(b)
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> lfSvc.addFlow(createLoopCommand, "addFlowTest"),
                "If source and target are the same, flow rejected and exception thrown");


        LogicalFlow newFlow = lfSvc.addFlow(createCommand, "addFlowTest");
        assertEquals(a, newFlow.source(), "Flow created should have the same source as in the create command");
        assertEquals(b, newFlow.target(), "Flow created should have the same target as in the create command");
        assertEquals("addFlowTest", newFlow.lastUpdatedBy(), "Flow created should have the user as the last updated by value");

    }


    @Test
    public void addFlows(){

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        dataTypeHelper.createUnknownDatatype();

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
        assertEquals(emptyList(), noCreateCommands, "If no list provided returns empty list");

        assertThrows(
                IllegalArgumentException.class,
                () -> lfSvc.addFlows(asList(createLoopCommand, abCreateCommand), "addFlowTest"),
                "If contains invalid flow (same src and trg) throws exception");

        List<LogicalFlow> newFlows = lfSvc.addFlows(asList(abCreateCommand, baCreateCommand), "addFlowsTest");
        assertEquals(2, newFlows.size(), "2 valid create commands should create 2 flows");

        assertEquals(asSet(tuple(a, b), tuple(b, a)),
                map(newFlows, f -> tuple(f.source(), f.target())),
                "Source and targets in returned set match");

        List<LogicalFlow> duplicatedFlows = lfSvc.addFlows(asList(bcCreateCommand, bcCreateCommand), "addFlowsTest");
        assertEquals(1,
                duplicatedFlows.size(),
                "multiple create commands for same source and target should not create multiple flows");

        assertEquals(asSet(tuple(b, c)),
                map(duplicatedFlows, f -> tuple(f.source(), f.target())),
                "multiple create commands for same source and target should not create multiple flows");

        List<LogicalFlow> existingFlows = lfSvc.addFlows(asList(bcCreateCommand, abCreateCommand), "addFlowsTest");
        assertTrue(existingFlows.isEmpty(), "should not create flow if flow already exists");
    }


    @Test
    public void restoreFlow(){

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = helper.createLogicalFlow(a, b);

        lfSvc.removeFlow(ab.id().get(), "restoreFlowTest");

        boolean restoreInvalid = lfSvc.restoreFlow(-1, "restoreFlowTest");

        assertFalse(restoreInvalid, "Cannot restore a flow that doesn't exist");

        boolean restoredFlow = lfSvc.restoreFlow(ab.id().get(), "restoreFlowTest");
        assertTrue(restoredFlow, "Restores flow if exists");

        LogicalFlow flow = lfSvc.getById(ab.id().get());

        assertNotEquals(
                "Restored flow should not have a lifecycle status of 'REMOVED'",
                EntityLifecycleStatus.REMOVED.name(),
                flow.entityLifecycleStatus().name());

        assertFalse(flow.isRemoved(), "Restored flow should not be removed");

        assertEquals("restoreFlowTest", flow.lastUpdatedBy(), "Restored flow should have last updated by user");
    }


    @Test
    public void cleanupOrphans(){

        helper.clearAllFlows();

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = helper.createLogicalFlow(a, b);
        LogicalFlow ac = helper.createLogicalFlow(a, c);
        LogicalFlow ca = helper.createLogicalFlow(c, a);

        int flowsRemoved = lfSvc.cleanupOrphans();

        assertEquals(0, flowsRemoved, "No flows removed if all apps are active");

        appHelper.removeApp(c.id());

        int flowsRemovedAfterAppRemoved = lfSvc.cleanupOrphans();

        assertEquals(2,
                flowsRemovedAfterAppRemoved,
                "Flows removed where either source or target is retired");

        LogicalFlow flowWhereTargetRemoved = lfSvc.getById(ac.id().get());

        assertEquals(EntityLifecycleStatus.REMOVED,
                flowWhereTargetRemoved.entityLifecycleStatus(),
                "If target removed, flow still exists but has entity lifecycle status of 'REMOVED'");
    }


    @Test
    public void cleanupSelfReferencingFlows() {

        helper.clearAllFlows();

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        int removedFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals(0, removedFlows, "Nothing removed if no logical flows");

        LogicalFlow ab = helper.createLogicalFlow(a, b);

        int removedWhereNoSelfReferencingFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals(0, removedWhereNoSelfReferencingFlows, "Nothing removed if no self-referencing logical flows");

        LogicalFlow aa = helper.createLogicalFlow(a, a);
        LogicalFlow bb = helper.createLogicalFlow(b, b);

        int removedAllWhereSelfReferencingFlows = lfSvc.cleanupSelfReferencingFlows();
        assertEquals(2, removedAllWhereSelfReferencingFlows, "Removed all self-referencing logical flows");

        LogicalFlow aaFlow = lfSvc.getById(aa.id().get());
        assertEquals(EntityLifecycleStatus.REMOVED, aaFlow.entityLifecycleStatus(), "Self referencing flow still exists but is removed");
    }


    @Test
    public void findUpstreamFlowsForEntityReferences() {

        helper.clearAllFlows();

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        EntityReference d = appHelper.createNewApp("d", ouIds.b);

        helper.createLogicalFlow(a, b);
        helper.createLogicalFlow(c, b);
        helper.createLogicalFlow(b, c);
        helper.createLogicalFlow(b, d);

        Collection<LogicalFlow> upstreamFlowsForEmptyList = lfSvc.findUpstreamFlowsForEntityReferences(emptyList());

        assertTrue(isEmpty(upstreamFlowsForEmptyList), "No upstreams when no ref provided");

        Collection<LogicalFlow> upstreamsWhereOnlySource = lfSvc.findUpstreamFlowsForEntityReferences(asList(a));
        assertEquals(0, upstreamsWhereOnlySource.size(), "No upstreams when all refs are only targets");

        Collection<LogicalFlow> allUpstreams = lfSvc.findUpstreamFlowsForEntityReferences(asList(b, c));
        assertEquals(3, allUpstreams.size(), "Returns all upstreams but not downstreams");
    }

}