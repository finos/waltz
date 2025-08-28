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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MakerCheckerServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    MakerCheckerService makerCheckerService;

    @Autowired
    ProposedFlowDao proposedFlowDao;

    @Autowired
    LogicalFlowDao logicalFlowDao;

    @Autowired
    PhysicalSpecificationDao physicalSpecificationDao;

    @Autowired
    EntityWorkflowStateDao entityWorkflowStateDao;

    @Autowired
    EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;

    private static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";

    @Test
    public void testProposedNewFlow() {

        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reason\": {\n" +
                "        \"description\": \"test\",\n" +
                "          \"ratingId\": 1\n" +
                "     },\n" +
                "    \"logicalFlowId\": 12345,\n" +
                "    \"physicalFlowId\": 12345,\n" +
                "    \"specification\": {\n" +
                "        \"owningEntity\": {\n" +
                "            \"id\": 18703,\n" +
                "            \"kind\": \"APPLICATION\",\n" +
                "            \"name\": \"AMG\",\n" +
                "            \"externalId\": \"60487-1\",\n" +
                "            \"description\": \"Business IT Management with utilising core functions of: \\r\\nEnterprise Architecture Management tool for IT Planning\",\n" +
                "            \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "        },\n" +
                "        \"name\": \"mc_specification\",\n" +
                "        \"description\": \"mc_specification description\",\n" +
                "        \"format\": \"DATABASE\",\n" +
                "        \"lastUpdatedBy\": \"waltz\",\n" +
                "        \"externalId\": \"mc-extId001\",\n" +
                "        \"id\": null\n" +
                "    },\n" +
                "    \"flowAttributes\": {\n" +
                "        \"name\": \"mc_deliverCharacterstics\",\n" +
                "        \"transport\": \"DATABASE_CONNECTION\",\n" +
                "        \"frequency\": \"BIANNUALLY\",\n" +
                "        \"basisOffset\": -30,\n" +
                "        \"criticality\": \"HIGH\",\n" +
                "        \"description\": \"mc-deliver-description\",\n" +
                "        \"externalId\": \"mc-deliver-ext001\"\n" +
                "    },\n" +
                "    \"dataTypeIds\": [\n" +
                "        41200\n" +
                "    ]\n" +
                "}";

        try {
            ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
            ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(requestBody, "testUser", command);
            assertNotNull(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetProposedFlowDefinition() {

        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reason\": {\n" +
                "        \"description\": \"test\",\n" +
                "          \"ratingId\": 1\n" +
                "     },\n" +
                "    \"logicalFlowId\": 12345,\n" +
                "    \"physicalFlowId\": 12345,\n" +
                "    \"specification\": {\n" +
                "        \"owningEntity\": {\n" +
                "            \"id\": 18703,\n" +
                "            \"kind\": \"APPLICATION\",\n" +
                "            \"name\": \"AMG\",\n" +
                "            \"externalId\": \"60487-1\",\n" +
                "            \"description\": \"Business IT Management with utilising core functions of: \\r\\nEnterprise Architecture Management tool for IT Planning\",\n" +
                "            \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "        },\n" +
                "        \"name\": \"mc_specification\",\n" +
                "        \"description\": \"mc_specification description\",\n" +
                "        \"format\": \"DATABASE\",\n" +
                "        \"lastUpdatedBy\": \"waltz\",\n" +
                "        \"externalId\": \"mc-extId001\",\n" +
                "        \"id\": null\n" +
                "    },\n" +
                "    \"flowAttributes\": {\n" +
                "        \"name\": \"mc_deliverCharacterstics\",\n" +
                "        \"transport\": \"DATABASE_CONNECTION\",\n" +
                "        \"frequency\": \"BIANNUALLY\",\n" +
                "        \"basisOffset\": -30,\n" +
                "        \"criticality\": \"HIGH\",\n" +
                "        \"description\": \"mc-deliver-description\",\n" +
                "        \"externalId\": \"mc-deliver-ext001\"\n" +
                "    },\n" +
                "    \"dataTypeIds\": [\n" +
                "        41200\n" +
                "    ]\n" +
                "}";

        try {
            ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
            ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(requestBody, "testUser", command);
            assertNotNull(response);

            ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(response.proposedFlowId());
            assertNotNull(proposedFlowResponse);
            assertNotNull((proposedFlowResponse.workflowState().workflowId()));
            assertTrue(proposedFlowResponse.workflowTransitionList().size() > 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWhenLogicalFlowIdIsNotPresentThenCreatesBothLogicalAndPhysicalFlow() throws JsonProcessingException, FlowCreationException {

        // 1. Arrange ----------------------------------------------------------
        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reason\": {\n" +
                "        \"description\": \"test\",\n" +
                "          \"ratingId\": 1\n" +
                "     },\n" +
                "    \"specification\": {\n" +
                "        \"owningEntity\": {\n" +
                "            \"id\": 18703,\n" +
                "            \"kind\": \"APPLICATION\",\n" +
                "            \"name\": \"AMG\",\n" +
                "            \"externalId\": \"60487-1\",\n" +
                "            \"description\": \"Business IT Management with utilising core functions of: \\r\\nEnterprise Architecture Management tool for IT Planning\",\n" +
                "            \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "        },\n" +
                "        \"name\": \"mc_specification\",\n" +
                "        \"description\": \"mc_specification description\",\n" +
                "        \"format\": \"DATABASE\",\n" +
                "        \"lastUpdatedBy\": \"waltz\",\n" +
                "        \"externalId\": \"mc-extId001\",\n" +
                "        \"id\": null\n" +
                "    },\n" +
                "    \"flowAttributes\": {\n" +
                "        \"name\": \"mc_deliverCharacterstics\",\n" +
                "        \"transport\": \"DATABASE_CONNECTION\",\n" +
                "        \"frequency\": \"BIANNUALLY\",\n" +
                "        \"basisOffset\": -30,\n" +
                "        \"criticality\": \"HIGH\",\n" +
                "        \"description\": \"mc-deliver-description\",\n" +
                "        \"externalId\": \"mc-deliver-ext001\"\n" +
                "    },\n" +
                "    \"dataTypeIds\": [\n" +
                "        41200\n" +
                "    ]\n" +
                "}";

        String username = "testUser";
        ProposedFlowCommand proposedFlowCommand = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
        Long proposedFlowId = proposedFlowDao.saveProposedFlow(requestBody, username, proposedFlowCommand);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(proposedFlowId, entityWorkflowDefId, username, entityReference.kind(), workflowState, description);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowId, username);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertNotNull(resp.logicalFlow().id());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }

    @Test
    void testWhenLogicalFlowAlreadyExistsThenOnlyPhysicalFlowIsCreated() throws JsonProcessingException, FlowCreationException {
        // 1. Arrange ----------------------------------------------------------
        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reason\": {\n" +
                "        \"description\": \"test\",\n" +
                "          \"ratingId\": 1\n" +
                "     },\n" +
                "    \"logicalFlowId\": 1,\n" +
                "    \"specification\": {\n" +
                "        \"owningEntity\": {\n" +
                "            \"id\": 18703,\n" +
                "            \"kind\": \"APPLICATION\",\n" +
                "            \"name\": \"AMG\",\n" +
                "            \"externalId\": \"60487-1\",\n" +
                "            \"description\": \"Business IT Management with utilising core functions of: \\r\\nEnterprise Architecture Management tool for IT Planning\",\n" +
                "            \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "        },\n" +
                "        \"name\": \"mc_specification\",\n" +
                "        \"description\": \"mc_specification description\",\n" +
                "        \"format\": \"DATABASE\",\n" +
                "        \"lastUpdatedBy\": \"waltz\",\n" +
                "        \"externalId\": \"mc-extId001\",\n" +
                "        \"id\": null\n" +
                "    },\n" +
                "    \"flowAttributes\": {\n" +
                "        \"name\": \"mc_deliverCharacterstics\",\n" +
                "        \"transport\": \"DATABASE_CONNECTION\",\n" +
                "        \"frequency\": \"BIANNUALLY\",\n" +
                "        \"basisOffset\": -30,\n" +
                "        \"criticality\": \"HIGH\",\n" +
                "        \"description\": \"mc-deliver-description\",\n" +
                "        \"externalId\": \"mc-deliver-ext001\"\n" +
                "    },\n" +
                "    \"dataTypeIds\": [\n" +
                "        41200\n" +
                "    ]\n" +
                "}";

        String username = "testUser";
        ProposedFlowCommand proposedFlowCommand = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
        Long proposedFlowId = proposedFlowDao.saveProposedFlow(requestBody, username, proposedFlowCommand);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(proposedFlowId, entityWorkflowDefId, username, entityReference.kind(), workflowState, description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(username)
                .created(UserTimestamp.mkForUser(username, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), username);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), proposedFlowCommand.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }

    private AddLogicalFlowCommand mapProposedFlowToAddLogicalFlowCommand(ProposedFlowResponse proposedFlow) {
        return ImmutableAddLogicalFlowCommand.builder()
                .source(proposedFlow.flowDef().source())
                .target(proposedFlow.flowDef().target())
                .build();
    }

    @Test
    void testPhysicalSpecificationCreationWhenPhysicalFlowIsCreated() throws FlowCreationException, JsonProcessingException {

        // 1. Arrange ----------------------------------------------------------
        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reason\": {\n" +
                "        \"description\": \"test\",\n" +
                "          \"ratingId\": 1\n" +
                "     },\n" +
                "    \"logicalFlowId\": 1,\n" +
                "    \"specification\": {\n" +
                "        \"owningEntity\": {\n" +
                "            \"id\": 18703,\n" +
                "            \"kind\": \"APPLICATION\",\n" +
                "            \"name\": \"AMG\",\n" +
                "            \"externalId\": \"60487-1\",\n" +
                "            \"description\": \"Business IT Management with utilising core functions of: \\r\\nEnterprise Architecture Management tool for IT Planning\",\n" +
                "            \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "        },\n" +
                "        \"name\": \"mc_specification\",\n" +
                "        \"description\": \"mc_specification description\",\n" +
                "        \"format\": \"DATABASE\",\n" +
                "        \"lastUpdatedBy\": \"waltz\",\n" +
                "        \"externalId\": \"mc-extId001\",\n" +
                "        \"id\": null\n" +
                "    },\n" +
                "    \"flowAttributes\": {\n" +
                "        \"name\": \"mc_deliverCharacterstics\",\n" +
                "        \"transport\": \"DATABASE_CONNECTION\",\n" +
                "        \"frequency\": \"BIANNUALLY\",\n" +
                "        \"basisOffset\": -30,\n" +
                "        \"criticality\": \"HIGH\",\n" +
                "        \"description\": \"mc-deliver-description\",\n" +
                "        \"externalId\": \"mc-deliver-ext001\"\n" +
                "    },\n" +
                "    \"dataTypeIds\": [\n" +
                "        41200\n" +
                "    ]\n" +
                "}";

        String username = "testUser";
        ProposedFlowCommand proposedFlowCommand = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
        Long proposedFlowId = proposedFlowDao.saveProposedFlow(requestBody, username, proposedFlowCommand);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(proposedFlowId, entityWorkflowDefId, username, entityReference.kind(), workflowState, description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(username)
                .created(UserTimestamp.mkForUser(username, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        PhysicalSpecification specification = ImmutablePhysicalSpecification
                .copyOf(proposedFlowResponse.flowDef().specification())
                .withLastUpdatedBy(username)
                .withLastUpdatedAt(now)
                .withCreated(UserTimestamp.mkForUser(username, now));

        long specId = physicalSpecificationDao.create(specification);
        PhysicalSpecification physicalSpecification = physicalSpecificationDao.getById(specId);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), username);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), proposedFlowCommand.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);

        assertNotNull(physicalSpecification);
        assertTrue(physicalSpecification.id().get() > 0L);
    }
}