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
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
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
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.model.entity_search.EntitySearchOptions.mkForEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNull("If unknown id returns null", unknownId);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "getById");

        PhysicalSpecification spec = psSvc.getById(specId);

        assertEquals("getById returns correct physical specification with correct owning entity", a, spec.owningEntity());
        assertTrue("getById returns correct physical specification", spec.name().startsWith("getById"));
    }

    @Test
    public void findByEntityReference() {
        assertThrows("Throws exception if entity reference is null",
                IllegalArgumentException.class,
                () -> psSvc.findByEntityReference(null));

        Set<PhysicalSpecification> whereEntityUnrelated = psSvc.findByEntityReference(mkRef(EntityKind.COST_KIND, -1));
        assertEquals("Returns an empty set where entity is not related", emptySet(), whereEntityUnrelated);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        Set<PhysicalSpecification> returnsAsSetEvenIfMultipleLinksToSpec = psSvc.findByEntityReference(a);

        assertEquals("Should return if no flows but is owning entity",
                asSet(a.id()),
                map(returnsAsSetEvenIfMultipleLinksToSpec, d -> d.owningEntity().id()));

        Set<PhysicalSpecification> noFlows = psSvc.findByEntityReference(b);
        assertEquals("Should return empty set for an application if not owner or linked by flow", emptySet(), noFlows);

        PhysicalFlowCreateCommandResponse physFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, mkName("findByEntityReference"));

        Set<PhysicalSpecification> linkedByPhysFlow = psSvc.findByEntityReference(b);
        assertEquals("Should return specs for an application if linked to a flow",
                asSet(specId),
                map(linkedByPhysFlow, d -> d.entityReference().id()));
    }


    @Test
    public void markRemovedIfUnused(){

        String username = mkName("markRemovedIfUnused");

        assertThrows("Throws exception if entity reference is null",
                IllegalArgumentException.class,
                () -> psSvc.markRemovedIfUnused(null, username));

        ImmutablePhysicalSpecificationDeleteCommand noSpecCmd = ImmutablePhysicalSpecificationDeleteCommand.builder().specificationId(-1L).build();

        CommandResponse<PhysicalSpecificationDeleteCommand> responseNoSpec = psSvc.markRemovedIfUnused(noSpecCmd, username);
        assertEquals("Fails to mark removed if spec not found", CommandOutcome.FAILURE, responseNoSpec.outcome());
        assertEquals("Should inform of reason for failure", "Specification not found", responseNoSpec.message().get());

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        PhysicalFlowCreateCommandResponse physicalFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, mkName("markRemovedIfUnused"));

        ImmutablePhysicalSpecificationDeleteCommand deleteCmd = ImmutablePhysicalSpecificationDeleteCommand.builder().specificationId(specId).build();
        CommandResponse<PhysicalSpecificationDeleteCommand> responseWithUnderlyingFlows = psSvc.markRemovedIfUnused(deleteCmd, username);
        assertEquals("Should not remove spec if underlying flows", CommandOutcome.FAILURE, responseWithUnderlyingFlows.outcome());
        assertEquals("Should inform of reason for failure", "This specification cannot be deleted as it is being referenced by one or more physical flows", responseWithUnderlyingFlows.message().get());

        pfHelper.deletePhysicalFlow(physicalFlow.entityReference().id());

        CommandResponse<PhysicalSpecificationDeleteCommand> responseWithNoUnderlyingFlows = psSvc.markRemovedIfUnused(deleteCmd, username);
        assertEquals("Should not remove spec if underlying flows", CommandOutcome.SUCCESS, responseWithNoUnderlyingFlows.outcome());
    }


    @Test
    public void findByIds(){
        assertEquals("Returns empty list if id list is null", emptyList(), psSvc.findByIds(null));
        assertEquals("Returns empty list if empty list provided", emptyList(), psSvc.findByIds(emptyList()));
        assertEquals("Returns empty set if empty ids cannot be found", emptyList(), psSvc.findByIds(asList(-1L)));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        Long specId2 = psHelper.createPhysicalSpec(b, "findByEntityReference");

        Collection<PhysicalSpecification> specs = psSvc.findByIds(asList(specId, specId2));
        assertEquals("Returns correct list of ids", asSet(specId, specId2), map(specs, d -> d.entityReference().id()));

        psHelper.removeSpec(specId2);
        Collection<PhysicalSpecification> specsAfterRemoval = psSvc.findByIds(asList(specId, specId2));
        assertEquals("Returns all specs by id, even if removed", asSet(specId, specId2), map(specsAfterRemoval, d -> d.entityReference().id()));

        Collection<PhysicalSpecification> withNullInList = psSvc.findByIds(asList(specId, null));
        assertEquals("Returns specs for all ids that are not null", asSet(specId), map(withNullInList, d -> d.entityReference().id()));
    }


    @Test
    public void updateExternalId() {
        assertThrows("Should throw exception if spec id is null",
                IllegalArgumentException.class,
                () -> psSvc.updateExternalId(null, null));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        assertThrows("Throws exception if new external id is null",
                IllegalArgumentException.class,
                () -> psSvc.updateExternalId(specId, null));

        int unknownSpec = psSvc.updateExternalId(-1L, "UPDATED");
        assertEquals("Should be no updates where unknown spec", 0, unknownSpec);

        int updatedSpecCount = psSvc.updateExternalId(specId, "UPDATED");
        assertEquals("Should be one update where spec exists", 1, updatedSpecCount);

        PhysicalSpecification updatedSpec = psSvc.getById(specId);
        assertEquals("Spec id should be updated", "UPDATED", updatedSpec.externalId().get());
    }


    @Test
    public void makeActive() {
        String username = mkName("makeActive");

        assertThrows("Should throw exception if spec id is null",
                IllegalArgumentException.class,
                () -> psSvc.makeActive(null, username));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        int active = psSvc.makeActive(specId, username);
        assertEquals("Will still update even if the spec is already active", 1, active);

        psHelper.removeSpec(specId);
        int madeActive = psSvc.makeActive(specId, username);
        assertEquals("Will update removed flows to active", 1, madeActive);

        assertThrows("Should throw exception if username is null as cannot be logged",
                IllegalArgumentException.class,
                () -> psSvc.makeActive(specId, null));

    }


    @Test
    public void propagateDataTypesToLogicalFlows() {

        String username = mkName("propagateDataTypesToLogicalFlows");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");
        pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, username);

        assertThrows("Should throw exception if username is null as cannot be logged",
                IllegalArgumentException.class,
                () -> psSvc.propagateDataTypesToLogicalFlows(null, specId));

        Long dt1Id = dtHelper.createDataType("dt1");
        Long dt2Id = dtHelper.createDataType("dt2");

        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(dt1Id));
        psSvc.propagateDataTypesToLogicalFlows(username, specId);

        List<DataTypeDecorator> lfDecorators = lfHelper.fetchDecoratorsForFlow(flow.entityReference().id());
        assertEquals("Propagating does not remove data types from the logical",
                asSet(dt1Id),
                map(lfDecorators, DataTypeDecorator::dataTypeId));

        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dt1Id), emptySet());
        psSvc.propagateDataTypesToLogicalFlows(username, specId);

        assertEquals("Can handle data types that already exist on the logical flow",
                asSet(dt1Id),
                map(lfDecorators, DataTypeDecorator::dataTypeId));

        dtdSvc.updateDecorators(username, mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId), asSet(dt2Id), emptySet());
        psSvc.propagateDataTypesToLogicalFlows(username, specId);
        List<DataTypeDecorator> lfDecoratorsWithSpecDts = lfHelper.fetchDecoratorsForFlow(flow.entityReference().id());

        assertEquals("Adds data types that are not currently on the logical flow",
                asSet(dt1Id, dt2Id),
                map(lfDecoratorsWithSpecDts, DataTypeDecorator::dataTypeId));
    }


    @Test
    public void create(){
        String username = mkName("create");

        assertThrows("Should throw exception if create command is null",
                IllegalArgumentException.class,
                () -> psSvc.create(null));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);

        String name = mkName("create");

        ImmutablePhysicalSpecification createCommand = ImmutablePhysicalSpecification.builder()
                .externalId(name)
                .owningEntity(a)
                .name(name)
                .description("desc")
                .format(DataFormatKind.UNKNOWN)
                .lastUpdatedBy(username)
                .isRemoved(false)
                .created(UserTimestamp.mkForUser(username, DateTimeUtilities.nowUtcTimestamp()))
                .build();

        Long specId = psSvc.create(createCommand);

        PhysicalSpecification spec = psSvc.getById(specId);
        assertEquals("Spec id should be created", specId, spec.id().get());
        assertEquals("Spec id should be have the same externalId", name, spec.name());

        assertThrows("Cannot create a specification with an id that exists",
                IllegalArgumentException.class,
                () -> psSvc.create(ImmutablePhysicalSpecification.copyOf(spec)));

    }


    @Test
    public void isUsed() {
        assertThrows("Specification id must not be null",
                IllegalArgumentException.class,
                () -> psSvc.isUsed(null));

        String username = mkName("propagateDataTypesToLogicalFlows");
        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        EntityReference b = appHelper.createNewApp("b", ouIds.a1);

        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        assertFalse("Should return false when so physical flows", psSvc.isUsed(specId));

        PhysicalFlowCreateCommandResponse physicalFlow = pfHelper.createPhysicalFlow(flow.entityReference().id(), specId, username);
        assertTrue("Should return false when associated physical flows", psSvc.isUsed(specId));

        pfHelper.deletePhysicalFlow(physicalFlow.entityReference().id());
        assertFalse("Should return false when all physical flows are removed", psSvc.isUsed(specId));
    }


    @Test
    public void findBySelector() {
        assertThrows("Options cannot be null",
                IllegalArgumentException.class,
                () -> psSvc.findBySelector(null));

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "findByEntityReference");

        IdSelectionOptions specOpts = mkOpts(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId));

        Collection<PhysicalSpecification> specs = psSvc.findBySelector(specOpts);
        assertEquals("When selector is a spec only returns one result", 1, specs.size());
        assertEquals("Returns spec when using spec selector", specId, first(specs).id().get());

        EntityReference b = appHelper.createNewApp("b", ouIds.a1);
        LogicalFlow flow = lfHelper.createLogicalFlow(a, b);
        Long specId2 = psHelper.createPhysicalSpec(b, "findByEntityReference");

        PhysicalFlowCreateCommandResponse physFlow = pfHelper
                .createPhysicalFlow(flow.entityReference().id(), specId, mkName("findBySelector"));

        PhysicalFlowCreateCommandResponse physFlow2 = pfHelper
                .createPhysicalFlow(flow.entityReference().id(), specId2, mkName("findBySelector"));

        IdSelectionOptions appOpts = mkOpts(mkRef(EntityKind.APPLICATION, b.id()));
        assertThrows("Throws exception  for unsupported entity kinds",
                UnsupportedOperationException.class,
                () -> psSvc.findBySelector(appOpts));

        IdSelectionOptions flowOpts = mkOpts(mkRef(EntityKind.LOGICAL_DATA_FLOW, flow.entityReference().id()));
        Collection<PhysicalSpecification> specsForFlow = psSvc.findBySelector(flowOpts);
        assertEquals("Returns all specs linked to a flow", 2, specsForFlow.size());
        assertEquals("Returns all specs linked to a flow",
                asSet(specId, specId2),
                map(specsForFlow, d -> d.entityReference().id()));

        pfHelper.deletePhysicalFlow(physFlow2.entityReference().id());
        psHelper.removeSpec(specId2);

        Collection<PhysicalSpecification> activeSpecsForFlow = psSvc.findBySelector(flowOpts);
        assertEquals("Returns all specs linked to a flow", 1, activeSpecsForFlow.size());
        assertEquals("Returns only active specs linked to a flow",
                asSet(specId),
                map(activeSpecsForFlow, d -> d.entityReference().id()));
    }


    @Test
    public void search() {
        assertThrows("Search options cannot be null",
                IllegalArgumentException.class,
                () -> psSvc.search(null));

        List<PhysicalSpecification> emptyQry = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, ""));
        assertEquals("Entity qry should return empty results", emptyList(), emptyQry);


        List<PhysicalSpecification> noResults = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals("Entity qry should return empty results", emptyList(), noResults);

        EntityReference a = appHelper.createNewApp("a", ouIds.a);
        Long specId = psHelper.createPhysicalSpec(a, "search");

        List<PhysicalSpecification> result = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals("Entity qry should return results which match",
                asSet(specId),
                map(result, r -> r.entityReference().id()));

        Long specId2 = psHelper.createPhysicalSpec(a, "search");

        List<PhysicalSpecification> multipleSpecs = psSvc.search(mkForEntity(EntityKind.PHYSICAL_SPECIFICATION, "search"));
        assertEquals("Entity qry should return results which match",
                asSet(specId, specId2), map(multipleSpecs,
                        r -> r.entityReference().id()));

    }

}