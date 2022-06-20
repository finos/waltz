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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.*;
import org.finos.waltz.model.*;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.*;
import org.finos.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionChangeCommand;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionType;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.junit.jupiter.api.Assertions.*;

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

        assertNull(unknownId, "If unknown id returns null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);

        Long specId = psHelper.createPhysicalSpec(a, "getById");

        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                flow.entityReference().id(),
                specId,
                mkName("getById"));

        PhysicalFlow physicalFlow = pfSvc.getById(physicalFlowResponse.entityReference().id());

        assertEquals(physicalFlowResponse.entityReference().id(), physicalFlow.entityReference().id(), "getById returns correct physical flow");
    }


    @Test
    public void findByExternalId() {

        List<PhysicalFlow> listFromNullExtId = pfSvc.findByExternalId(null);
        assertEquals(emptyList(), listFromNullExtId, "If null ext id returns empty list");

        List<PhysicalFlow> listFromUnknownExtId = pfSvc.findByExternalId(mkName("findByExternalId"));
        assertEquals(emptyList(), listFromUnknownExtId, "If ext id doesn't exist returns empty list");

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

        assertEquals(1, physicalFlows.size(), "findByExternalIds returns correct number of physical flows");
        assertEquals(asSet(physicalFlowResponse.entityReference().id()),
                map(physicalFlows, d -> d.entityReference().id()),
                "findByExternalIds returns correct physical flows");

        Long specId2 = psHelper.createPhysicalSpec(a, "findByExternalId2");

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                flow.entityReference().id(),
                specId2,
                mkName("findByExternalId2"));

        extIdHelper.createExtId(physicalFlowResponse2.entityReference(), "findByExternalId", mkName("findByExternalId2"));
        List<PhysicalFlow> twoPhysicalFlows = pfSvc.findByExternalId("findByExternalId");

        assertEquals(2, twoPhysicalFlows.size(), "findByExternalIds returns correct number of physical flows");
        assertEquals(asSet(physicalFlowResponse.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(twoPhysicalFlows, d -> d.entityReference().id()),
                "findByExternalIds returns correct physical flows");
    }


    @Test
    public void findByEntityReference() {

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.findByEntityReference(null),
                "Null entity reference throws an IllegalArgumentException");

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
        assertEquals(emptyList(), entityNotExists, "Returns empty list when entity doesn't exist");

        List<PhysicalFlow> entityExists = pfSvc.findByEntityReference(mkRef(EntityKind.APPLICATION, a.id()));
        assertEquals(2, entityExists.size(), "Returns correct number of flows for entity");
        assertEquals(asSet(physicalFlowResponse1.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(entityExists, d -> d.entityReference().id()),
                "Returns correct flows for entity");

        List<PhysicalFlow> correctFlowsForEntity = pfSvc.findByEntityReference(mkRef(EntityKind.APPLICATION, b.id()));
        assertEquals(1, correctFlowsForEntity.size(), "Returns correct number of flows for entity");
        assertEquals(asSet(physicalFlowResponse1.entityReference().id()),
                map(correctFlowsForEntity, d -> d.entityReference().id()),
                "Returns correct flows for entity");
    }

    @Test
    public void findByProducerEntityReference() {

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.findByProducerEntityReference(null),
                "Null entity reference throws an IllegalArgumentException");

        List<PhysicalFlow> entityNotExists = pfSvc.findByProducerEntityReference(mkRef(EntityKind.APPLICATION, -1));
        assertEquals(emptyList(), entityNotExists, "Returns empty list when entity doesn't exist");

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
        assertEquals(2, physicalsDownstreamOfA.size(), "Returns all flows downstream of a but not upstream flows");
        assertEquals(asSet(physicalFlowResponse1.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(physicalsDownstreamOfA, d -> d.entityReference().id()),
                "Returns expected flows downstream of a");

        List<PhysicalFlow> physicalsDownstreamOfC = pfSvc.findByProducerEntityReference(c);
        assertEquals(asSet(physicalFlowResponse3.entityReference().id()),
                map(physicalsDownstreamOfC, d -> d.entityReference().id()),
                "Returns expected flows downstream of c");

        List<PhysicalFlow> physicalsDownstreamOfB = pfSvc.findByProducerEntityReference(b);
        assertEquals(emptySet(),
                map(physicalsDownstreamOfB, d -> d.entityReference().id()),
                "Returns expected flows downstream of b");
    }


    @Test
    public void findByConsumerEntityReference() {

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.findByConsumerEntityReference(null),
                "Null entity reference throws an IllegalArgumentException");

        List<PhysicalFlow> entityNotExists = pfSvc.findByConsumerEntityReference(mkRef(EntityKind.APPLICATION, -1));
        assertEquals(emptyList(), entityNotExists, "Returns empty list when entity doesn't exist");

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
        assertEquals(emptySet(),
                map(physicalsUpstreamOfA, d -> d.entityReference().id()),
                "Returns expected flows upstream of a");

        List<PhysicalFlow> physicalsUpstreamOfC = pfSvc.findByConsumerEntityReference(c);
        assertEquals(2, physicalsUpstreamOfC.size(), "Returns all flows upstream of a but not downstream flows");
        assertEquals(asSet(physicalFlowResponse2.entityReference().id(), physicalFlowResponse3.entityReference().id()),
                map(physicalsUpstreamOfC, d -> d.entityReference().id()),
                "Returns expected flows upstream of c");

        List<PhysicalFlow> physicalsDownstreamOfB = pfSvc.findByConsumerEntityReference(b);
        assertEquals(asSet(physicalFlowResponse1.entityReference().id()),
                map(physicalsDownstreamOfB, d -> d.entityReference().id()),
                "Returns expected flows upstream of b");
    }

    @Test
    public void findBySpecificationId() {

        List<PhysicalFlow> unknownSpecId = pfSvc.findBySpecificationId(-1);
        assertEquals(emptyList(), unknownSpecId, "If unknown spec id returns empty list");

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
        assertEquals(asSet(physicalFlowResponse.entityReference().id()),
                map(physicalFlows, d -> d.entityReference().id()),
                "findBySpecificationId returns correct physical flows");

        PhysicalFlowCreateCommandResponse physicalFlowResponse2 = pfHelper.createPhysicalFlow(
                bc.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        PhysicalFlowCreateCommandResponse physicalFlowResponse3 = pfHelper.createPhysicalFlow(
                ac.entityReference().id(),
                specId2,
                mkName("findBySpecificationId"));

        List<PhysicalFlow> physicalFlows2 = pfSvc.findBySpecificationId(specId);
        assertEquals(asSet(physicalFlowResponse.entityReference().id(), physicalFlowResponse2.entityReference().id()),
                map(physicalFlows2, d -> d.entityReference().id()),
                "findBySpecificationId returns only physical flows with that spec");
    }

    @Test
    public void findBySelector() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pfSvc.findBySelector(null), "Null selection options should throw IllegalArgumentException");

        assertThrows(
                NullPointerException.class,
                () -> pfSvc.findBySelector(mkOpts(null)), "Null entity reference should throw NullPointerException when constructing options");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        Collection<PhysicalFlow> bySelectorWithNoFlows = pfSvc.findBySelector(mkOpts(a));
        assertEquals(emptyList(), bySelectorWithNoFlows, "Returns empty list when no flows for selector");

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findBySpecificationId");
        PhysicalFlowCreateCommandResponse physicalFlowResponse = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        Collection<PhysicalFlow> bySelectorWithFlows = pfSvc.findBySelector(mkOpts(a));
        assertEquals(asSet(physicalFlowResponse.entityReference().id()),
                map(bySelectorWithFlows, d -> d.entityReference().id()),
                "Returns correct flows for selector");

        EntityReference c = appHelper.createNewApp("c", ouIds.b);
        LogicalFlow bc = lfHelper.createLogicalFlow(b, c);
        PhysicalFlowCreateCommandResponse otherFlowLinkedToSpec = pfHelper.createPhysicalFlow(
                bc.entityReference().id(),
                specId,
                mkName("findBySpecificationId"));

        Collection<PhysicalFlow> bySpecSelectorWithRelatedFlows = pfSvc.findBySelector(mkOpts(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId)));
        assertEquals(asSet(physicalFlowResponse.entityReference().id(), otherFlowLinkedToSpec.entityReference().id()),
                map(bySpecSelectorWithRelatedFlows, d -> d.entityReference().id()),
                "Returns correct flows for spec selector");

        Collection<PhysicalFlow> byLogFlowSelectorWithRelatedFlows = pfSvc.findBySelector(mkOpts(ab.entityReference()));
        assertEquals(asSet(physicalFlowResponse.entityReference().id()),
                map(byLogFlowSelectorWithRelatedFlows, d -> d.entityReference().id()),
                "Returns correct flows for log flow selector");

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

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.delete(null, mkName("deletingFlow")),
                "Should throw exception if passed in a null phys flow id");

        PhysicalFlowDeleteCommandResponse invalidFlowIdResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(-1)
                        .build(),
                mkName("deletingFlow"));

        assertEquals(CommandOutcome.FAILURE, invalidFlowIdResp.outcome(), "Should return failure if the flow does not exist");

        PhysicalFlowDeleteCommandResponse deletedFlowResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(physFlow.entityReference().id())
                        .build(),
                mkName("deletingFlow"));

        PhysicalFlow physicalFlowAfterDeletion = pfSvc.getById(physFlow.entityReference().id());

        assertEquals(CommandOutcome.SUCCESS, deletedFlowResp.outcome(), "Should be successful if flow was deleted");
        assertTrue(physicalFlowAfterDeletion.isRemoved(), "Physical flow should be soft deleted with isRemoved set true");

        PhysicalFlowDeleteCommandResponse reDeletingFlowResp = pfSvc.delete(ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(physFlow.entityReference().id())
                        .build(),
                mkName("deletingFlow"));

        assertEquals(CommandOutcome.FAILURE,
                reDeletingFlowResp.outcome(),
                "Should fail when trying to delete a flow that is already removed");
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

        assertEquals(CommandOutcome.FAILURE,
                deletedFlowResp.outcome(),
                "Physical flow should not be deleted if marked read only");
    }


    @Test
    public void create() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")),
                "Should throw exception if null object passed into create");


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
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
        assertEquals(CommandOutcome.SUCCESS, createResp.outcome(), "Can successfully create physical flows");

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.create(createCommandInvalidFlowId, mkName("create")),
                "Throws an exception when trying to create a physical flow for an invalid logical flow id");

    }


    @Test
    public void create_shouldRestoreLogicalFlowIfRemoved() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")),
                "Should throw exception if null object passed into create");


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
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
        assertEquals(CommandOutcome.SUCCESS, createRespWithRemovedFlow.outcome(), "Should restore removed logical flows and create physical");

        LogicalFlow abAfterFlowAdded = lfSvc.getById(ab.entityReference().id());
        assertFalse(abAfterFlowAdded.isRemoved(), "Logical flows should be active if physicals have been added");
    }


    @Test
    public void create_allowsSimilarFlowToBeCreatedIfOriginalWasRemoved() {

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("delete"));
        PhysicalSpecification spec = psSvc.getById(specId);

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.create(null, mkName("create")),
                "Should throw exception if null object passed into create");


        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
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

        assertTrue(pf1.isRemoved(), "First flows should remain removed");
        assertFalse(pf2.isRemoved(), "Second flow should be active");

        assertEquals(CommandOutcome.SUCCESS,
                secondCreateResp.outcome(),
                "Can successfully create physical flows sharing attributes if the first physical similar was removed");
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
                .format(DataFormatKindValue.UNKNOWN)
                .lastUpdatedBy(username)
                .isRemoved(false)
                .created(UserTimestamp.mkForUser(username, DateTimeUtilities.nowUtcTimestamp()))
                .build();

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(newSpec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse firstCreateResp = pfSvc.create(createCommand, username);

        assertEquals(CommandOutcome.SUCCESS, firstCreateResp.outcome(), "Successfully creates flow when new spec needs to be created");

        PhysicalFlow newPhysFlow = pfSvc.getById(firstCreateResp.entityReference().id());
        PhysicalSpecification createdSpec = psSvc.getById(newPhysFlow.specificationId());

        assertEquals(specExtId, createdSpec.externalId().get(), "Specification associated to new flow has the correct external id");
        assertEquals(username, createdSpec.created().get().by(), "Specification associated to flow has the correct user against it");
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
        assertTrue(specOnceRemoved.isRemoved(), "Specification is only soft deleted prior to reactivation");

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand createCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(specOnceRemoved)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse createResp = pfSvc.create(createCommand, username);
        assertEquals(CommandOutcome.SUCCESS, createResp.outcome(), "Successfully creates flow when spec needs to be reactivated");

        PhysicalFlow newPhysFlow = pfSvc.getById(createResp.entityReference().id());
        PhysicalSpecification reactivatedSpec = psSvc.getById(newPhysFlow.specificationId());

        assertFalse(reactivatedSpec.isRemoved(), "Specification is active after new flow is created");
        assertEquals(specId, Long.valueOf(reactivatedSpec.entityReference().id()), "Specification associated to flow has the correct id");
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
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
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
        assertEquals(CommandOutcome.SUCCESS, firstCreateResp.outcome(), "Successfully creates flow when new");

        PhysicalFlowCreateCommandResponse secondCreateResp = pfSvc.create(createCommand2, username);
        assertEquals(CommandOutcome.FAILURE, secondCreateResp.outcome(), "Fails to create flow when a similar one is already active");

        assertTrue(secondCreateResp.message()
                        .map(msg -> msg.equalsIgnoreCase("Duplicate with existing flow"))
                        .orElse(false),
                "Failure message advises user that there is an existing flow");
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
        assertEquals(Optional.empty(), physicalFlow.specificationDefinitionId(), "Check the physical flow has been created without correct spec id");

        assertThrows(IllegalArgumentException.class,
                () -> pfSvc.updateSpecDefinitionId(mkName("updateSpecDefinitionId"), physicalFlow.id().get(), null),
                "Should throw an IllegalArgumentException if the update command is null");

        int updated = pfSvc.updateSpecDefinitionId(
                mkName("updateSpecDefinitionId"),
                physicalFlow.id().get(),
                ImmutablePhysicalFlowSpecDefinitionChangeCommand.builder().newSpecDefinitionId(specDefnId).build());

        assertEquals(1, updated, "Should successfully update the specification id to the new spec");

        PhysicalFlow flowWithNewSpec = pfSvc.getById(flowCreateResp.entityReference().id());
        assertEquals(Long.valueOf(specDefnId), flowWithNewSpec.specificationDefinitionId().get(), "Should successfully update the specification id to the new spec");
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

        assertTrue(firstFlowAfterUpdate.isRemoved(), "Once merged the original flow should no longer be active");

        assertTrue(map(flowsWithMergeExtId, d -> d.entityReference().id()).contains(secondFlowCreateResp.entityReference().id()),
                "Once merged the second flow should have the external id of the original");

        PhysicalSpecification specAfterUpdate = psSvc.getById(specId);
        assertTrue(specAfterUpdate.isRemoved(), "If last remaining flow spec should be removed");
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
        assertFalse(specAfterUpdate.isRemoved(), "If other flows share the spec it should not be removed after the merge");
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

        assertTrue(secondFlowAfterUpdate.externalId().map(extId -> extId.equalsIgnoreCase("merge2")).orElse(false), "If the target flow already had an ext Id this is retained");
        assertTrue(map(flowsWithMergeExtId, d -> d.entityReference().id()).contains(secondFlowCreateResp.entityReference().id()),
                "Once merged the second flow should have the external id of the original");

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
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .build();

        ImmutablePhysicalFlowCreateCommand createCmd = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttributes)
                .build();

        PhysicalFlowCreateCommandResponse flowResp = pfSvc.create(createCmd, mkName("updateAttribute"));

        assertThrows(UnsupportedOperationException.class,
                () -> pfSvc.updateAttribute(
                        mkName("updateAttribute"),
                        ImmutableSetAttributeCommand.builder()
                                .name("invalidAttr")
                                .entityReference(flowResp.entityReference())
                                .value("invalid")
                                .build()),
                "If attribute name is not recognised throw exception");

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

        assertEquals("after", physFlowAfterUpdates.description(), "Description should be updated after updateAttribute");
        assertEquals(FrequencyKindValue.of("MONTHLY"), physFlowAfterUpdates.frequency(), "Frequency should be updated after updateAttribute");
        assertEquals(TransportKindValue.of("OTHER"), physFlowAfterUpdates.transport(), "Transport should be updated after updateAttribute");
        assertEquals(0, physFlowAfterUpdates.basisOffset(), "Basis offset should be updated after updateAttribute");
        assertEquals(EntityLifecycleStatus.REMOVED, physFlowAfterUpdates.entityLifecycleStatus(), "EntityLifecycleStatus should be updated after updateAttribute");
        assertEquals(CriticalityValue.of("LOW"), physFlowAfterUpdates.criticality(), "Criticality should be updated after updateAttribute");
    }


    @Test
    public void findUnderlyingPhysicalFlows() {

        Collection<PhysicalFlowInfo> underlyingPhysicalFlowsWhenLogicalNull = pfSvc.findUnderlyingPhysicalFlows(null);
        assertTrue(isEmpty(underlyingPhysicalFlowsWhenLogicalNull), "If null logical flow id then returns empty list");

        Collection<PhysicalFlowInfo> underlyingPhysicalFlowsWhenLogicalDoesntExist = pfSvc.findUnderlyingPhysicalFlows(-1L);
        assertTrue(isEmpty(underlyingPhysicalFlowsWhenLogicalDoesntExist), "If invalid logical flow id then returns empty list");

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.a1);

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);

        Collection<PhysicalFlowInfo> noPhysicalsAssociated = pfSvc.findUnderlyingPhysicalFlows(ab.entityReference().id());
        assertTrue(isEmpty(noPhysicalsAssociated), "If no physicals associated to the logical then returns empty list");

        Long specId = psHelper.createPhysicalSpec(a, mkName("updateAttribute"));
        PhysicalFlowCreateCommandResponse flowCreateResp = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findUnderlyingPhysicalFlows"));

        Collection<PhysicalFlowInfo> physicalAssociated = pfSvc.findUnderlyingPhysicalFlows(ab.entityReference().id());
        assertEquals(1, physicalAssociated.size(), "Returns correct number of underlying physical flows for that logical");

        pfHelper.deletePhysicalFlow(flowCreateResp.entityReference().id());

        Collection<PhysicalFlowInfo> noActivePhysicals = pfSvc.findUnderlyingPhysicalFlows(ab.entityReference().id());
        assertTrue(isEmpty(noActivePhysicals), "If no active physicals associated to the logical then returns empty list");

        PhysicalFlowCreateCommandResponse flowCreateResp2 = pfHelper.createPhysicalFlow(
                ab.entityReference().id(),
                specId,
                mkName("findUnderlyingPhysicalFlows"));

        Collection<PhysicalFlowInfo> physicalAssociated2 = pfSvc.findUnderlyingPhysicalFlows(ab.entityReference().id());
        assertEquals(1, physicalAssociated2.size(), "Returns correct number of underlying physical flows for that logical");

        lfSvc.removeFlow(ab.entityReference().id(), mkName("findUnderlyingPhysicalFlows"));

        Collection<PhysicalFlowInfo> underlyingPhysicalsForRemovedLogical = pfSvc.findUnderlyingPhysicalFlows(ab.entityReference().id());
        assertTrue(isEmpty(underlyingPhysicalsForRemovedLogical), "Returns empty list of the logical flow is removed");
    }
}