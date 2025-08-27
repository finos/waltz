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
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.junit.jupiter.api.Assertions.*;


public class MakerCheckerServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    MakerCheckerService makerCheckerService;

    @Autowired
    ProposedFlowDao proposedFlowDao;

    @Autowired
    LogicalFlowDao logicalFlowDao;

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

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowId, username);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertNotNull(resp.logicalFlow().id());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }

    @Test
    void testWhenLogicalFlowAlreadyExistsThenOnlyPhysicalFlowIsCreated_1() throws JsonProcessingException, FlowCreationException {
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
}