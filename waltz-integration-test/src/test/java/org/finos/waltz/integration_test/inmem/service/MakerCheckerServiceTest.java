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
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowDefinition;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MakerCheckerServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    MakerCheckerService makerCheckerService;

    @Test
    public void testProposedNewFlow(){
        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reasonCode\": 1234,\n" +
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

        try{
            ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
            ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(requestBody, "testUser", command);
            assertNotNull(response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void testGetProposedFlowDefinition()
    {
        String requestBody = "{\n" +
                "    \"source\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 101\n" +
                "    },\n" +
                "    \"target\": {\n" +
                "        \"kind\": \"APPLICATION\",\n" +
                "        \"id\": 202\n" +
                "    },\n" +
                "    \"reasonCode\": 1234,\n" +
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

        try{
            ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);
            ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(requestBody, "testUser", command);
            assertNotNull(response);
            Optional<ProposedFlowDefinition> proposedFlowDefinition = makerCheckerService.getProposedFlowDefinition(response.proposedFlowId());
            assertNotNull(proposedFlowDefinition);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}