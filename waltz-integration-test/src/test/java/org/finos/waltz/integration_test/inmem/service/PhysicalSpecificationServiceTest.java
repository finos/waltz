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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.physical_specification.*;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.test_common.helpers.*;
import org.finos.waltz.test_common_again.helpers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.model.entity_search.EntitySearchOptions.mkForEntity;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

public class PhysicalSpecificationServiceTest extends BaseInMemoryIntegrationTest {


    @Autowired
    private PhysicalSpecificationService psSvc;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private PhysicalSpecHelper psHelper;

    @Autowired
    private PhysicalFlowHelper pfHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DataTypeHelper dtHelper;

    @Autowired
    private DataTypeDecoratorService dtdSvc;


    @Test
    public void getById() {

        PhysicalSpecification unknownId = psSvc.getById(-1);
        assertNull(unknownId, "If unknown id returns null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "getById");

        PhysicalSpecification spec = psSvc.getById(specId);

        assertEquals(a, spec.owningEntity(), "getById returns correct physical specification with correct owning entity");
        assertTrue(spec.name().startsWith("getById"), "getById returns correct physical specification");
    }

    @Test
    public void findByEntityReference() {
        assertThrows(IllegalArgumentException.class,
                () -> psSvc.findByEntityReference(null),
                "Throws exception if entity reference is null");

        Set<PhysicalSpecification> whereEntityUnrelated = psSvc.findByEntityReference(mkRef(EntityKind.COST_KIND, -1));
        assertEquals(emptySet(), whereEntityUnrelated, "Returns an empty set where entity is not related");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        Set<PhysicalSpecification> returnsAsSetEvenIfMultipleLinksToSpec = psSvc.findByEntityReference(a);

        assertEquals(asSet(a.id()),
                map(returnsAsSetEvenIfMultipleLinksToSpec, d -> d.owningEntity().id()),
                "Should return if no flows but is owning entity");

        Set<PhysicalSpecification> noFlows = psSvc.findByEntityReference(b);
        assertEquals(emptySet(), noFlows, "Should return empty set for an application if not owner or linked by flow");

        PhysicalFlowCreateCommandResponse physFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, mkName("findByEntityReference"));

        Set<PhysicalSpecification> linkedByPhysFlow = psSvc.findByEntityReference(b);
        assertEquals(asSet(specId),
                map(linkedByPhysFlow, d -> d.entityReference().id()),
                "Should return specs for an application if linked to a flow");
    }


    @Test
    public void markRemovedIfUnused() {

        String username = mkName("markRemovedIfUnused");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.markRemovedIfUnused(null, username),
                "Throws exception if entity reference is null");

        ImmutablePhysicalSpecificationDeleteCommand noSpecCmd = ImmutablePhysicalSpecificationDeleteCommand.builder().specificationId(-1L).build();

        CommandResponse<PhysicalSpecificationDeleteCommand> responseNoSpec = psSvc.markRemovedIfUnused(noSpecCmd, username);
        assertEquals(CommandOutcome.FAILURE, responseNoSpec.outcome(), "Fails to mark removed if spec not found");
        assertEquals("Specification not found", responseNoSpec.message().get(), "Should inform of reason for failure");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        PhysicalFlowCreateCommandResponse physicalFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, mkName("markRemovedIfUnused"));

        ImmutablePhysicalSpecificationDeleteCommand deleteCmd = ImmutablePhysicalSpecificationDeleteCommand.builder().specificationId(specId).build();
        CommandResponse<PhysicalSpecificationDeleteCommand> responseWithUnderlyingFlows = psSvc.markRemovedIfUnused(deleteCmd, username);
        assertEquals(CommandOutcome.FAILURE, responseWithUnderlyingFlows.outcome(), "Should not remove spec if underlying flows");
        assertEquals("This specification cannot be deleted as it is being referenced by one or more physical flows", responseWithUnderlyingFlows.message().get(), "Should inform of reason for failure");

        pfHelper.deletePhysicalFlow(physicalFlow.entityReference().id());

        CommandResponse<PhysicalSpecificationDeleteCommand> responseWithNoUnderlyingFlows = psSvc.markRemovedIfUnused(deleteCmd, username);
        assertEquals(CommandOutcome.SUCCESS, responseWithNoUnderlyingFlows.outcome(), "Should not remove spec if underlying flows");
    }


    @Test
    public void findByIds() {
        assertEquals(emptyList(), psSvc.findByIds(null), "Returns empty list if id list is null");
        assertEquals(emptyList(), psSvc.findByIds(emptyList()), "Returns empty list if empty list provided");
        assertEquals(emptyList(), psSvc.findByIds(asList(-1L)), "Returns empty set if empty ids cannot be found");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        Long specId2 = psHelper.createPhysicalSpec(b, "findByEntityReference");

        Collection<PhysicalSpecification> specs = psSvc.findByIds(asList(specId, specId2));
        assertEquals(asSet(specId, specId2), map(specs, d -> d.entityReference().id()), "Returns correct list of ids");

        psHelper.removeSpec(specId2);
        Collection<PhysicalSpecification> specsAfterRemoval = psSvc.findByIds(asList(specId, specId2));
        assertEquals(asSet(specId, specId2), map(specsAfterRemoval, d -> d.entityReference().id()), "Returns all specs by id, even if removed");

        Collection<PhysicalSpecification> withNullInList = psSvc.findByIds(asList(specId, null));
        assertEquals(asSet(specId), map(withNullInList, d -> d.entityReference().id()), "Returns specs for all ids that are not null");
    }


    @Test
    public void updateExternalId() {
        assertThrows(IllegalArgumentException.class,
                () -> psSvc.updateExternalId(null, null),
                "Should throw exception if spec id is null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.updateExternalId(specId, null),
                "Throws exception if new external id is null");

        int unknownSpec = psSvc.updateExternalId(-1L, "UPDATED");
        assertEquals(0, unknownSpec, "Should be no updates where unknown spec");

        int updatedSpecCount = psSvc.updateExternalId(specId, "UPDATED");
        assertEquals(1, updatedSpecCount, "Should be one update where spec exists");

        PhysicalSpecification updatedSpec = psSvc.getById(specId);
        assertEquals("UPDATED", updatedSpec.externalId().get(), "Spec id should be updated");
    }


    @Test
    public void makeActive() {
        String username = mkName("makeActive");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.makeActive(null, username),
                "Should throw exception if spec id is null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        int active = psSvc.makeActive(specId, username);
        assertEquals(1, active, "Will still update even if the spec is already active");

        psHelper.removeSpec(specId);
        int madeActive = psSvc.makeActive(specId, username);
        assertEquals(1, madeActive, "Will update removed flows to active");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.makeActive(specId, null),
                "Should throw exception if username is null as cannot be logged");

    }


    @Test
    public void propagateDataTypesToLogicalFlows() {

        String username = mkName("propagateDataTypesToLogicalFlows");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, username);

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.propagateDataTypesToLogicalFlows(null, specId),
                "Should throw exception if username is null as cannot be logged");

        Long dt1Id = dtHelper.createDataType("dt1");
        Long dt2Id = dtHelper.createDataType("dt2");

        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(dt1Id));
        psSvc.propagateDataTypesToLogicalFlows(username, specId);

        List<DataTypeDecorator> lfDecorators = lfHelper.fetchDecoratorsForFlow(flow.entityReference().id());
        assertEquals(asSet(dt1Id),
                map(lfDecorators, DataTypeDecorator::dataTypeId),
                "Propagating does not remove data types from the logical");

        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dt1Id), emptySet());
        psSvc.propagateDataTypesToLogicalFlows(username, specId);

        assertEquals(asSet(dt1Id),
                map(lfDecorators, DataTypeDecorator::dataTypeId),
                "Can handle data types that already exist on the logical flow");

        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dt2Id), emptySet());
        psSvc.propagateDataTypesToLogicalFlows(username, specId);
        List<DataTypeDecorator> lfDecoratorsWithSpecDts = lfHelper.fetchDecoratorsForFlow(flow.entityReference().id());

        assertEquals(asSet(dt1Id, dt2Id),
                map(lfDecoratorsWithSpecDts, DataTypeDecorator::dataTypeId),
                "Adds data types that are not currently on the logical flow");
    }


    @Test
    public void create() {
        String username = mkName("create");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.create(null),
                "Should throw exception if create command is null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        String name = mkName("create");

        ImmutablePhysicalSpecification createCommand = ImmutablePhysicalSpecification.builder()
                .externalId(name)
                .owningEntity(a)
                .name(name)
                .description("desc")
                .format(DataFormatKindValue.UNKNOWN)
                .lastUpdatedBy(username)
                .isRemoved(false)
                .created(UserTimestamp.mkForUser(username, DateTimeUtilities.nowUtcTimestamp()))
                .build();

        Long specId = psSvc.create(createCommand);

        PhysicalSpecification spec = psSvc.getById(specId);
        assertEquals(specId, spec.id().get(), "Spec id should be created");
        assertEquals(name, spec.name(), "Spec id should be have the same externalId");

        assertThrows(IllegalArgumentException.class,
                () -> psSvc.create(ImmutablePhysicalSpecification.copyOf(spec)),
                "Cannot create a specification with an id that exists");

    }


    @Test
    public void isUsed() {
        assertThrows(IllegalArgumentException.class,
                () -> psSvc.isUsed(null),
                "Specification id must not be null");

        String username = mkName("propagateDataTypesToLogicalFlows");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        assertFalse(psSvc.isUsed(specId), "Should return false when so physical flows");

        PhysicalFlowCreateCommandResponse physicalFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, username);
        assertTrue(psSvc.isUsed(specId), "Should return false when associated physical flows");

        pfHelper.deletePhysicalFlow(physicalFlow.entityReference().id());
        assertFalse(psSvc.isUsed(specId), "Should return false when all physical flows are removed");
    }


    @Test
    public void findBySelector() {
        assertThrows(IllegalArgumentException.class,
                () -> psSvc.findBySelector(null),
                "Options cannot be null");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        IdSelectionOptions specOpts = mkOpts(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));

        Collection<PhysicalSpecification> specs = psSvc.findBySelector(specOpts);
        assertEquals(1, specs.size(), "When selector is a spec only returns one result");
        assertEquals(specId, first(specs).id().get(), "Returns spec when using spec selector");

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId2 = psHelper.createPhysicalSpec(b, "findByEntityReference");

        PhysicalFlowCreateCommandResponse physFlow = pfHelper
                .createPhysicalFlow(flow.entityReference().id(), specId, mkName("findBySelector"));

        PhysicalFlowCreateCommandResponse physFlow2 = pfHelper
                .createPhysicalFlow(flow.entityReference().id(), specId2, mkName("findBySelector"));

        IdSelectionOptions measurableRatingOpts = mkOpts(mkRef(EntityKind.MEASURABLE_RATING, 1L));
        assertThrows(UnsupportedOperationException.class,
                () -> psSvc.findBySelector(measurableRatingOpts),
                "Throws exception for unsupported entity kinds");

        IdSelectionOptions flowOpts = mkOpts(mkRef(EntityKind.LOGICAL_DATA_FLOW, flow.entityReference().id()));
        Collection<PhysicalSpecification> specsForFlow = psSvc.findBySelector(flowOpts);
        assertEquals(2, specsForFlow.size(), "Returns all specs linked to a flow");
        assertEquals(asSet(specId, specId2),
                map(specsForFlow, d -> d.entityReference().id()),
                "Returns all specs linked to a flow");

        pfHelper.deletePhysicalFlow(physFlow2.entityReference().id());
        psHelper.removeSpec(specId2);

        Collection<PhysicalSpecification> activeSpecsForFlow = psSvc.findBySelector(flowOpts);
        assertEquals(1, activeSpecsForFlow.size(), "Returns all specs linked to a flow");
        assertEquals(asSet(specId),
                map(activeSpecsForFlow, d -> d.entityReference().id()),
                "Returns only active specs linked to a flow");
    }


    @Test
    public void search() {
        assertThrows(IllegalArgumentException.class,
                () -> psSvc.search(null),
                "Search options cannot be null");

        List<PhysicalSpecification> emptyQry = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, ""));
        assertEquals(emptyList(), emptyQry, "Entity qry should return empty results");


        List<PhysicalSpecification> noResults = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals(emptyList(), noResults, "Entity qry should return empty results");

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "search");

        List<PhysicalSpecification> result = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals(asSet(specId),
                map(result, r -> r.entityReference().id()),
                "Entity qry should return results which match");

        Long specId2 = psHelper.createPhysicalSpec(a, "search");

        List<PhysicalSpecification> multipleSpecs = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals(asSet(specId, specId2),
                map(multipleSpecs,
                        r -> r.entityReference().id()), "Entity qry should return results which match");

    }

}