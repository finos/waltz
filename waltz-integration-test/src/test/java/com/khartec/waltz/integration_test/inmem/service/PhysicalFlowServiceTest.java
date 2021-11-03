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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import com.khartec.waltz.integration_test.inmem.helpers.*;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionType;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.*;

public class PhysicalFlowServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private PhysicalFlowService pfSvc;

    @Autowired
    private PhysicalSpecificationService psSvc;

    @Autowired
    private PhysicalSpecDefinitionService psdSvc;

    @Autowired
    private LogicalFlowService lfSvc;

    @Autowired
    private ExternalIdHelper extIdHelper;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private PhysicalSpecHelper psHelper;

    @Autowired
    private PhysicalFlowHelper pfHelper;

    @Autowired
    private AppHelper appHelper;

    @Test
    public void getById() {

        PhysicalFlow unknownId = pfSvc.getById(-1);

        assertNull("If unknown id returns null", unknownId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, "getById");

        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                flow.entityReference().id(),
                specId,
                mkName("getById"));

        PhysicalFlow physicalFlow = pfSvc.getById(physicalFlowResponse.entityReference().id());

        assertEquals("getById returns correct physical flow", physicalFlowResponse.entityReference().id(), physicalFlow.entityReference().id());
    }


    @Test
    public void findByExternalId() {

        List<PhysicalFlow> listFromNullExtId = pfSvc.findByExternalId(null);
        assertEquals("If null ext id returns empty list", emptyList(), listFromNullExtId);

        List<PhysicalFlow> listFromUnknownExtId = pfSvc.findByExternalId(mkName("findByExternalId"));
        assertEquals("If ext id doesn't exist returns empty list", emptyList(), listFromUnknownExtId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByExternalId");

        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                flow.entityReference().id(),
                specId,
                mkName("findByExternalId"));

        extIdHelper.createExtId(physicalFlowResponse.entityReference(), "findByExternalId", mkName("findByExternalId"));
        List<PhysicalFlow> physicalFlows = pfSvc.findByExternalId("findByExternalId");

        assertEquals("findByExternalIds returns correct number of physical flows", 1, physicalFlows.size());
        assertEquals("findByExternalIds returns correct physical flows",
                asSet(physicalFlowResponse.entityReference().id()),
                map(physicalFlows, d -> d.entityReference().id()));

        Long specId2 = psHelper.createPhysicalSpec(a, "findByExternalId2");

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                flow.entityReference().id(),
                specId2,
                mkName("findByExternalId2"));

        extIdHelper.createExtId(physicalFlowResponse2.entityReference(), "findByExternalId", mkName("findByExternalId2"));
        List<PhysicalFlow> twoPhysicalFlows = pfSvc.findByExternalId("findByExternalId");

        assertEquals("findByExternalIds returns correct number of physical flows", 2, twoPhysicalFlows.size());
        assertEquals("findByExternalIds returns correct physical flows",
                asSet(physicalFlowResponse.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(twoPhysicalFlows, d -> d.entityReference().id()));
    }


    @Test
    public void findByEntityReference() {

        assertThrows("Null entity reference throws an IllegalArgumentException",
                IllegalArgumentException.class,
                () -> pfSvc.findByEntityReference(null));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);

        Long specId = psHelper.createPhysicalSpec(a, "findByExternalId");

        PhysicalFlowCreateCommandResponse physicalFlowResponse1 = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findByExternalId"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                ac.entityReference().id(),
                specId,
                mkName("findByExternalId"));

        List<PhysicalFlow> entityNotExists = pfSvc.findByEntityReference(mkRef(EntityKind.APPLICATION, -1));
        assertEquals("Returns empty list when entity doesn't exist", emptyList(), entityNotExists);

        List<PhysicalFlow> entityExists = pfSvc.findByEntityReference(mkRef(EntityKind.APPLICATION, a.id()));
        assertEquals("Returns correct number of flows for entity", 2, entityExists.size());
        assertEquals("Returns correct flows for entity",
                asSet(physicalFlowResponse1.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(entityExists, d -> d.entityReference().id()));

        List<PhysicalFlow> correctFlowsForEntity = pfSvc.findByEntityReference(mkRef(EntityKind.APPLICATION, b.id()));
        assertEquals("Returns correct number of flows for entity", 1, correctFlowsForEntity.size());
        assertEquals("Returns correct flows for entity",
                asSet(physicalFlowResponse1.entityReference().id()),
                map(correctFlowsForEntity, d -> d.entityReference().id()));
    }

    @Test
    public void findByProducerEntityReference() {

        assertThrows("Null entity reference throws an IllegalArgumentException",
                IllegalArgumentException.class,
                () -> pfSvc.findByProducerEntityReference(null));

        List<PhysicalFlow> entityNotExists = pfSvc.findByProducerEntityReference(mkRef(EntityKind.APPLICATION, -1));
        assertEquals("Returns empty list when entity doesn't exist", emptyList(), entityNotExists);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);
        LogicalFlow ca = lfHelper.createLogicalFlow(c, a);

        Long specId = psHelper.createPhysicalSpec(a, "findByExternalId");

        PhysicalFlowCreateCommandResponse physicalFlowResponse1 = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findByProducerEntityReference"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                ac.entityReference().id(),
                specId,
                mkName("findByProducerEntityReference"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse3 = pfHelper.createPhysicalFlow(
                ca.entityReference().id(),
                specId,
                mkName("findByProducerEntityReference"));

        List<PhysicalFlow> physicalsDownstreamOfA = pfSvc.findByProducerEntityReference(a);
        assertEquals("Returns all flows downstream of a but not upstream flows", 2, physicalsDownstreamOfA.size());
        assertEquals("Returns expected flows downstream of a",
                asSet(physicalFlowResponse1.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(physicalsDownstreamOfA, d -> d.entityReference().id()));

        List<PhysicalFlow> physicalsDownstreamOfC = pfSvc.findByProducerEntityReference(c);
        assertEquals("Returns expected flows downstream of c",
                asSet(physicalFlowResponse3.entityReference().id()),
                map(physicalsDownstreamOfC, d -> d.entityReference().id()));

        List<PhysicalFlow> physicalsDownstreamOfB = pfSvc.findByProducerEntityReference(b);
        assertEquals("Returns expected flows downstream of b",
                emptySet(),
                map(physicalsDownstreamOfB, d -> d.entityReference().id()));
    }


    @Test
    public void findByConsumerEntityReference() {

        assertThrows("Null entity reference throws an IllegalArgumentException",
                IllegalArgumentException.class,
                () -> pfSvc.findByConsumerEntityReference(null));

        List<PhysicalFlow> entityNotExists = pfSvc.findByConsumerEntityReference(mkRef(EntityKind.APPLICATION, -1));
        assertEquals("Returns empty list when entity doesn't exist", emptyList(), entityNotExists);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);
        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);

        Long specId = psHelper.createPhysicalSpec(a, "findByExternalId");

        PhysicalFlowCreateCommandResponse physicalFlowResponse1 = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findByConsumerEntityReference"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                ac.entityReference().id(),
                specId,
                mkName("findByConsumerEntityReference"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse3 = pfHelper.createPhysicalFlow(
                bc.entityReference().id(),
                specId,
                mkName("findByConsumerEntityReference"));

        List<PhysicalFlow> physicalsUpstreamOfA = pfSvc.findByConsumerEntityReference(a);
        assertEquals("Returns expected flows upstream of a",
                emptySet(),
                map(physicalsUpstreamOfA, d -> d.entityReference().id()));

        List<PhysicalFlow> physicalsUpstreamOfC = pfSvc.findByConsumerEntityReference(c);
        assertEquals("Returns all flows upstream of a but not downstream flows", 2, physicalsUpstreamOfC.size());
        assertEquals("Returns expected flows upstream of c",
                asSet(physicalFlowResponse2.entityReference().id(), physicalFlowResponse3.entityReference().id()),
                map(physicalsUpstreamOfC, d -> d.entityReference().id()));

        List<PhysicalFlow> physicalsDownstreamOfB = pfSvc.findByConsumerEntityReference(b);
        assertEquals("Returns expected flows upstream of b",
                asSet(physicalFlowResponse1.entityReference().id()),
                map(physicalsDownstreamOfB, d -> d.entityReference().id()));
    }

    @Test
    public void findBySpecificationId() {

        List<PhysicalFlow> unknownSpecId = pfSvc.findBySpecificationId(-1);
        assertEquals("If unknown spec id returns empty list", emptyList(), unknownSpecId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        EntityReference c = appHelper.createNewApp("c", ouIds.b);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);
        LogicalFlow ac = lfHelper.createLogicalFlow(a, c);

        Long specId = psHelper.createPhysicalSpec(a, "findBySpecificationId");
        Long specId2 = psHelper.createPhysicalSpec(a, mkName("findBySpecificationId"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        List<PhysicalFlow> physicalFlows = pfSvc.findBySpecificationId(specId);
        assertEquals("findBySpecificationId returns correct physical flows",
                asSet(physicalFlowResponse.entityReference().id()),
                map(physicalFlows, d -> d.entityReference().id()));

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                bc.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse3 = pfHelper.createPhysicalFlow(
                ac.entityReference().id(),
                specId2,
                mkName("findBySpecificationId"));

        List<PhysicalFlow> physicalFlows2 = pfSvc.findBySpecificationId(specId);
        assertEquals("findBySpecificationId returns only physical flows with that spec",
                asSet(physicalFlowResponse.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(physicalFlows2, d -> d.entityReference().id()));
    }

    @Test
    public void findBySelector() {

        assertThrows(
                "Null selection options should throw IllegalArgumentException",
                IllegalArgumentException.class, () -> pfSvc.findBySelector(null));

        assertThrows(
                "Null entity reference should throw NullPointerException when constructing options",
                NullPointerException.class, () -> pfSvc.findBySelector(mkOpts(null)));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        Collection<PhysicalFlow> bySelectorWithNoFlows = pfSvc.findBySelector(mkOpts(a));
        assertEquals("Returns empty list when no flows for selector", emptyList(), bySelectorWithNoFlows);

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findBySpecificationId");
        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        Collection<PhysicalFlow> bySelectorWithFlows = pfSvc.findBySelector(mkOpts(a));
        assertEquals("Returns correct flows for selector",
                asSet(physicalFlowResponse.entityReference().id()),
                map(bySelectorWithFlows, d -> d.entityReference().id()));

        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);
        PhysicalFlowCreateCommandResponse otherFlowLinkedToSpec = pfHelper.createPhysicalFlow(
                bc.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        Collection<PhysicalFlow> bySpecSelectorWithRelatedFlows = pfSvc.findBySelector(mkOpts(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId)));
        assertEquals("Returns correct flows for spec selector",
                asSet(physicalFlowResponse.entityReference().id(), otherFlowLinkedToSpec.entityReference().id()),
                map(bySpecSelectorWithRelatedFlows, d -> d.entityReference().id()));

        Collection<PhysicalFlow> byLogFlowSelectorWithRelatedFlows = pfSvc.findBySelector(mkOpts(ab.entityReference()));
        assertEquals("Returns correct flows for log flow selector",
                asSet(physicalFlowResponse.entityReference().id()),
                map(byLogFlowSelectorWithRelatedFlows, d -> d.entityReference().id()));

    }


    @Test
    public void delete() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));

        PhysicalFlowCreateCommandResponse physFlow = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("delete"));

        assertThrows("Should throw exception if passed in a null phys flow id",
                IllegalArgumentException.class,
                () -> pfSvc.delete(null, mkName("deletingFlow")));

        PhysicalFlowDeleteCommandResponse invalidFlowIdResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(-1)
                        .build(),
                mkName("deletingFlow"));

        assertEquals("Should return failure if the flow does not exist", CommandOutcome.FAILURE, invalidFlowIdResp.outcome());

        PhysicalFlowDeleteCommandResponse deletedFlowResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(physFlow.entityReference().id())
                        .build(),
                mkName("deletingFlow"));

        PhysicalFlow physicalFlowAfterDeletion = pfSvc.getById(physFlow.entityReference().id());

        assertEquals("Should be successful if flow was deleted", CommandOutcome.SUCCESS, deletedFlowResp.outcome());
        assertTrue("Physical flow should be soft deleted with isRemoved set true", physicalFlowAfterDeletion.isRemoved());

        PhysicalFlowDeleteCommandResponse reDeletingFlowResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(physFlow.entityReference().id())
                        .build(),
                mkName("deletingFlow"));

        assertEquals("Should fail when trying to delete a flow that is already removed",
                CommandOutcome.FAILURE,
                reDeletingFlowResp.outcome());
    }


    @Test
    public void delete_cannotBeDeletedIfReadOnly() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));

        PhysicalFlowCreateCommandResponse physFlow = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("deleteReadOnly"));

        pfHelper.markFlowAsReadOnly(physFlow.entityReference().id());

        PhysicalFlowDeleteCommandResponse deletedFlowResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(physFlow.entityReference().id())
                        .build(),
                mkName("deleteReadOnly"));

        assertEquals("Physical flow should not be deleted if marked read only",
                CommandOutcome.FAILURE,
                deletedFlowResp.outcome());
    }


    @Test
    public void create() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows("Should throw exception if null object passed into create",
                IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")));


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommandInvalidFlowId = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(-1)
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse createResp = pfSvc.create(createCommand, mkName("create"));
        assertEquals("Can successfully create physical flows", CommandOutcome.SUCCESS, createResp.outcome());

        assertThrows("Throws an exception when trying to create a physical flow for an invalid logical flow id",
                IllegalArgumentException.class,
                () -> pfSvc.create(createCommandInvalidFlowId, mkName("create")));

    }


    @Test
    public void create_shouldRestoreLogicalFlowIfRemoved() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows("Should throw exception if null object passed into create",
                IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")));


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        lfSvc.removeFlow(ab.entityReference().id(), mkName("create"));

        PhysicalFlowCreateCommandResponse createRespWithRemovedFlow = pfSvc.create(createCommand, mkName("create"));
        assertEquals("Should restore removed logical flows and create physical", CommandOutcome.SUCCESS, createRespWithRemovedFlow.outcome());

        LogicalFlow abAfterFlowAdded = lfSvc.getById(ab.entityReference().id());
        assertFalse("Logical flows should be active if physicals have been added", abAfterFlowAdded.isRemoved());
    }


    @Test
    public void create_allowsSimilarFlowToBeCreatedIfOriginalWasRemoved() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows("Should throw exception if null object passed into create",
                IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")));


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse firstCreateResp = pfSvc.create(createCommand, mkName("createAllowsSimilarToBeCreatedInFuture"));
        PhysicalFlowDeleteCommandResponse deleteResp = pfHelper.deletePhysicalFlow(firstCreateResp.entityReference().id());
        PhysicalFlowCreateCommandResponse secondCreateResp = pfSvc.create(createCommand, mkName("createAllowsSimilarToBeCreatedInFuture"));

        PhysicalFlow pf1 = pfSvc.getById(firstCreateResp.entityReference().id());
        PhysicalFlow pf2 = pfSvc.getById(secondCreateResp.entityReference().id());

        assertTrue("First flows should remain removed", pf1.isRemoved());
        assertFalse("Second flow should be active", pf2.isRemoved());

        assertEquals("Can successfully create physical flows sharing attributes if the first physical similar was removed",
                CommandOutcome.SUCCESS,
                secondCreateResp.outcome());
    }


    @Test
    public void create_canCreateNewSpecIfRequired() {

        String username = mkName("createWillCreateSpecIfRequired");

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        String specExtId = mkName("createWillCreateSpecIfRequired");

        ImmutablePhysicalSpecification newSpec = ImmutablePhysicalSpecification.builder()
                .externalId(specExtId)
                .owningEntity(a)
                .name(specExtId)
                .description(specExtId)
                .format(DataFormatKind.UNKNOWN)
                .lastUpdatedBy(username)
                .isRemoved(false)
                .created(UserTimestamp.mkForUser(username, DateTimeUtilities.nowUtcTimestamp()))
                .build();

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(newSpec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse firstCreateResp = pfSvc.create(createCommand, username);

        assertEquals("Successfully creates flow when new spec needs to be created", CommandOutcome.SUCCESS, firstCreateResp.outcome());

        PhysicalFlow newPhysFlow = pfSvc.getById(firstCreateResp.entityReference().id());
        PhysicalSpecification createdSpec = psSvc.getById(newPhysFlow.specificationId());

        assertEquals("Specification associated to new flow has the correct external id", specExtId, createdSpec.externalId().get());
        assertEquals("Specification associated to flow has the correct user against it", username, createdSpec.created().get().by());
    }

    @Test
    public void create_canReactivateSpecIfRequired() {

        String username = mkName("createCanReactivateSpecIfRequired");

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("createIfDuplicateFlowWillReturnFailureWithMessage"));

        psSvc.markRemovedIfUnused(
                ImmutablePhysicalSpecificationDeleteCommand.builder()
                        .specificationId(specId)
                        .build(),
                username);

        PhysicalSpecification specOnceRemoved = psSvc.getById(specId);
        assertTrue("Specification is only soft deleted prior to reactivation", specOnceRemoved.isRemoved());

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(specOnceRemoved)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse createResp = pfSvc.create(createCommand, username);
        assertEquals("Successfully creates flow when spec needs to be reactivated", CommandOutcome.SUCCESS, createResp.outcome());

        PhysicalFlow newPhysFlow = pfSvc.getById(createResp.entityReference().id());
        PhysicalSpecification reactivatedSpec = psSvc.getById(newPhysFlow.specificationId());

        assertFalse("Specification is active after new flow is created", reactivatedSpec.isRemoved());
        assertEquals("Specification associated to flow has the correct id", specId, Long.valueOf(reactivatedSpec.entityReference().id()));
    }


    @Test
    public void create_ifDuplicateFlowWillReturnFailureWithMessage() {

        String username = mkName("createIfDuplicateFlowWillReturnFailureWithMessage");

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("createIfDuplicateFlowWillReturnFailureWithMessage"));
        PhysicalSpecification spec = psSvc.getById(specId);

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKind.DAILY)
                .criticality(Criticality.MEDIUM)
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand2 = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse firstCreateResp = pfSvc.create(createCommand, username);
        assertEquals("Successfully creates flow when new", CommandOutcome.SUCCESS, firstCreateResp.outcome());

        PhysicalFlowCreateCommandResponse secondCreateResp = pfSvc.create(createCommand2, username);
        assertEquals("Fails to create flow when a similar one is already active", CommandOutcome.FAILURE, secondCreateResp.outcome());

        assertTrue("Failure message advises user that there is an existing flow",
                secondCreateResp.message()
                        .map(msg -> msg.equalsIgnoreCase("Duplicate with existing flow"))
                        .orElse(false));
    }


    @Test
    public void updateSpecDefinitionId() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("updateSpecDefinitionId"));

        long specDefnId = psdSvc.create(
                mkName("updateSpecDefinitionId"),
                specId,
                ImmutablePhysicalSpecDefinitionChangeCommand.builder()
                        .version("updateSpecDefinitionId")
                        .type(PhysicalSpecDefinitionType.DELIMITED)
                        .status(ReleaseLifecycleStatus.ACTIVE)
                        .delimiter(";")
                        .build());

        PhysicalFlowCreateCommandResponse flowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId, mkName("updateSpecDefinitionId"));
        PhysicalFlow physicalFlow = pfSvc.getById(flowCreateResp.entityReference().id());
        assertEquals("Check the physical flow has been created without correct spec id", Optional.empty(), physicalFlow.specificationDefinitionId());

        assertThrows("Should throw an IllegalArgumentException if the update command is null",
                IllegalArgumentException.class,
                () -> pfSvc.updateSpecDefinitionId(mkName("updateSpecDefinitionId"), physicalFlow.id().get(), null));

        int updated = pfSvc.updateSpecDefinitionId(
                mkName("updateSpecDefinitionId"),
                physicalFlow.id().get(),
                ImmutablePhysicalFlowSpecDefinitionChangeCommand.builder().newSpecDefinitionId(specDefnId).build());

        assertEquals("Should successfully update the specification id to the new spec", 1, updated);

        PhysicalFlow flowWithNewSpec = pfSvc.getById(flowCreateResp.entityReference().id());
        assertEquals("Should successfully update the specification id to the new spec", Long.valueOf(specDefnId), flowWithNewSpec.specificationDefinitionId().get());
    }

    @Test
    public void merge() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("merge"));
        Long specId2 = psHelper.createPhysicalSpec(a, mkName("merge2"));

        PhysicalFlowCreateCommandResponse firstFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId, mkName("merge"));
        PhysicalFlowCreateCommandResponse secondFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId2, mkName("merge"));

        pfHelper.updateExternalIdOnFlowDirectly(firstFlowCreateResp.entityReference().id(), "merge");

        boolean merge = pfSvc.merge(firstFlowCreateResp.entityReference().id(), secondFlowCreateResp.entityReference().id(), mkName("merge"));

        List<PhysicalFlow> flowsWithMergeExtId = pfSvc.findByExternalId("merge");
        PhysicalFlow firstFlowAfterUpdate = pfSvc.getById(firstFlowCreateResp.entityReference().id());

        assertTrue("Once merged the original flow should no longer be active", firstFlowAfterUpdate.isRemoved());

        assertTrue("Once merged the second flow should have the external id of the original",
                map(flowsWithMergeExtId, d -> d.entityReference().id()).contains(secondFlowCreateResp.entityReference().id()));

        PhysicalSpecification specAfterUpdate = psSvc.getById(specId);
        assertTrue("If last remaining flow spec should be removed", specAfterUpdate.isRemoved());
    }


    @Test
    public void merge_ifSpecShareByMultipleFlowsShouldNotBeRemoved() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);
        EntityReference c = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);

        Long specId = psHelper.createPhysicalSpec(a, mkName("merge"));
        Long specId2 = psHelper.createPhysicalSpec(a, mkName("merge2"));

        PhysicalFlowCreateCommandResponse firstFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId, mkName("merge"));
        PhysicalFlowCreateCommandResponse secondFlowCreateResp = pfHelper.createPhysicalFlow(bc.entityReference().id(), specId, mkName("merge"));
        PhysicalFlowCreateCommandResponse thirdFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId2, mkName("merge"));

        boolean merge = pfSvc.merge(firstFlowCreateResp.entityReference().id(), thirdFlowCreateResp.entityReference().id(), mkName("merge"));

        PhysicalSpecification specAfterUpdate = psSvc.getById(specId);
        assertFalse("If other flows share the spec it should not be removed after the merge", specAfterUpdate.isRemoved());
    }


    @Test
    public void merge_canCreateAdditionalExtIdsInTheExternalIdentifierTableIfNeeded() {
        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("merge"));
        Long specId2 = psHelper.createPhysicalSpec(a, mkName("merge2"));

        PhysicalFlowCreateCommandResponse firstFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId, mkName("merge"));
        PhysicalFlowCreateCommandResponse secondFlowCreateResp = pfHelper.createPhysicalFlow(ab.entityReference().id(), specId2, mkName("merge"));

        pfHelper.updateExternalIdOnFlowDirectly(firstFlowCreateResp.entityReference().id(), "merge");
        pfHelper.updateExternalIdOnFlowDirectly(secondFlowCreateResp.entityReference().id(), "merge2");

        boolean merge = pfSvc.merge(firstFlowCreateResp.entityReference().id(), secondFlowCreateResp.entityReference().id(), mkName("merge"));

        List<PhysicalFlow> flowsWithMergeExtId = pfSvc.findByExternalId("merge");
        PhysicalFlow firstFlowAfterUpdate = pfSvc.getById(firstFlowCreateResp.entityReference().id());
        PhysicalFlow secondFlowAfterUpdate = pfSvc.getById(secondFlowCreateResp.entityReference().id());

        assertTrue("If the target flow already had an ext Id this is retained", secondFlowAfterUpdate.externalId().map(extId -> extId.equalsIgnoreCase("merge2")).orElse(false));
        assertTrue("Once merged the second flow should have the external id of the original",
                map(flowsWithMergeExtId, d -> d.entityReference().id()).contains(secondFlowCreateResp.entityReference().id()));

    }

    @Test
    public void updateAttribute() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, mkName("updateAttribute"));
        PhysicalSpecification spec = psSvc.getById(specId);

        ImmutableFlowAttributes flowAttributes = ImmutableFlowAttributes.builder()
                .transport(TransportKindValue.UNKNOWN)
                .description("before")
                .basisOffset(1)
                .criticality(Criticality.MEDIUM)
                .frequency(FrequencyKind.DAILY)
                .build();

        ImmutablePhysicalFlowCreateCommand createCmd = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttributes)
                .build();

        PhysicalFlowCreateCommandResponse flowResp = pfSvc.create(createCmd, mkName("updateAttribute"));

        assertThrows("If attribute name is not recognised throw exception",
                UnsupportedOperationException.class,
                () -> pfSvc.updateAttribute(
                        mkName("updateAttribute"),
                        ImmutableSetAttributeCommand.builder()
                                .name("invalidAttr")
                                .entityReference(flowResp.entityReference())
                                .value("invalid")
                                .build()));

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("description")
                        .entityReference(flowResp.entityReference())
                        .value("after")
                        .build());

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("frequency")
                        .entityReference(flowResp.entityReference())
                        .value("MONTHLY")
                        .build());

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("criticality")
                        .entityReference(flowResp.entityReference())
                        .value("LOW")
                        .build());

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("entity_lifecycle_status")
                        .entityReference(flowResp.entityReference())
                        .value("REMOVED")
                        .build());

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("basisOffset")
                        .entityReference(flowResp.entityReference())
                        .value("0")
                        .build());

        pfSvc.updateAttribute(
                mkName("updateAttribute"),
                ImmutableSetAttributeCommand.builder()
                        .name("transport")
                        .entityReference(flowResp.entityReference())
                        .value("OTHER")
                        .build());

        PhysicalFlow physFlowAfterUpdates = pfSvc.getById(flowResp.entityReference().id());

        assertEquals("Description should be updated after updateAttribute", "after", physFlowAfterUpdates.description());
        assertEquals("Frequency should be updated after updateAttribute", FrequencyKind.MONTHLY, physFlowAfterUpdates.frequency());
        assertEquals("Transport should be updated after updateAttribute", TransportKindValue.of("OTHER"), physFlowAfterUpdates.transport());
        assertEquals("Basis offset should be updated after updateAttribute", 0, physFlowAfterUpdates.basisOffset());
        assertEquals("EntityLifecycleStatus should be updated after updateAttribute", EntityLifecycleStatus.REMOVED, physFlowAfterUpdates.entityLifecycleStatus());
        assertEquals("Criticality should be updated after updateAttribute", Criticality.LOW, physFlowAfterUpdates.criticality());
    }
}