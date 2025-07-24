package org.finos.waltz.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.finos.waltz.common.JacksonUtilities;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.finos.waltz.service.makerchecker.MakerCheckerService;
import org.finos.waltz.web.endpoints.api.MakerCheckerEndpoint;
import org.finos.waltz.web.endpoints.extracts.OrgUnitExtractor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MakerCheckerEndpointTest {

    MakerCheckerEndpoint makerCheckerEndpoint;

    @Mock
    MakerCheckerService makerCheckerService;
    @Mock
    private Request request;
    @Mock
    private Response response;

    @Mock
    private WebUtilities webUtilities;

    @BeforeEach
    public void setUp(){
        makerCheckerEndpoint = new MakerCheckerEndpoint(makerCheckerService);
    }


    @Test
    void testEndpoint() throws IOException {
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

            when(request.attribute("waltz-user")).thenReturn("testUser");
            when(request.bodyAsBytes()).thenReturn(requestBody.getBytes());

            List<EntityWorkflowTransition> list = new ArrayList<>();
            when(makerCheckerService.proposedNewFlow(any(),any(),any())).thenReturn(list);

            List<EntityWorkflowTransition> result = makerCheckerEndpoint.proposeNewFlow(request,response);
            assertNotNull(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}