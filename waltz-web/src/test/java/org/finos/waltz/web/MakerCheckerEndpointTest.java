package org.finos.waltz.web;

import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowDefinition;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.finos.waltz.web.endpoints.api.MakerCheckerEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Optional;

import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                "\n" +
                "  \"specification\": {\n" +
                "\n" +
                "    \"owningEntity\": {\n" +
                "\n" +
                "      \"id\": 20506,\n" +
                "\n" +
                "      \"kind\": \"APPLICATION\"\n" +
                "\n" +
                "    },\n" +
                "\n" +
                "    \"name\": \"Shreyans Jain\",\n" +
                "\n" +
                "    \"description\": \"\",\n" +
                "\n" +
                "    \"format\": \"UNKNOWN\",\n" +
                "\n" +
                "    \"lastUpdatedBy\": \"waltz\",\n" +
                "\n" +
                "    \"externalId\": null,\n" +
                "\n" +
                "    \"id\": null\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  \"flowAttributes\": {\n" +
                "\n" +
                "    \"name\": \"sss\",\n" +
                "\n" +
                "    \"transport\": \"UNKNOWN\",\n" +
                "\n" +
                "    \"frequency\": \"QUARTERLY\",\n" +
                "\n" +
                "    \"basisOffset\": 0,\n" +
                "\n" +
                "    \"criticality\": \"NONE\",\n" +
                "\n" +
                "    \"description\": \"\",\n" +
                "\n" +
                "    \"externalId\": null\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  \"logicalFlowId\": null,\n" +
                "\n" +
                "  \"physicalFlowId\": null,\n" +
                "\n" +
                "  \"dataTypeIds\": [\n" +
                "\n" +
                "    44400,\n" +
                "\n" +
                "    82084\n" +
                "\n" +
                "  ],\n" +
                "\n" +
                "  \"reason\": {\n" +
                "\n" +
                "    \"ratingId\": 42770,\n" +
                "\n" +
                "    \"description\": \"Approved data flow in an external system\"\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  \"source\": {\n" +
                "\n" +
                "    \"id\": 20506,\n" +
                "\n" +
                "    \"kind\": \"APPLICATION\",\n" +
                "\n" +
                "    \"name\": \"Waltz\",\n" +
                "\n" +
                "    \"externalId\": \"109235-1\",\n" +
                "\n" +
                "    \"description\": \"Architecture tool to aggregate information from different source. To provide reporting and visualization across CT for applications, tech and data for both senior management and the architecture community.\",\n" +
                "\n" +
                "    \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  \"target\": {\n" +
                "\n" +
                "    \"description\": \"db-GHS I(Geneos Hosting Services) s a Redhat virtualised platform that will host Geneos Gateway  instances to provide Hardware and Application monitoring throughout the bank. Geneos is the standard tool within Deutsche Bank for application monitoring. It is a full featured and fully customizable application monitoring tool. A powerful and customisable console enables real-time and historical metrics from application and infrastructure components to be visualised in an application, infrastructure or business centric way.  Notification of threshold breaches to support teams is automated though email, SMS, Symphony and dbUnity incident ticket as well as visually in the console.  Automated corrective actions, links to knowledge articles, operator commands and dashboards are just a few more of its many features.\\r\\n\\r\\nConfluence Link  -  https://confluence.intranet.db.com/display/GENEOS/Home\",\n" +
                "\n" +
                "    \"kind\": \"APPLICATION\",\n" +
                "\n" +
                "    \"id\": 20798,\n" +
                "\n" +
                "    \"name\": \"db-GHS\",\n" +
                "\n" +
                "    \"externalId\": \"97626-1\",\n" +
                "\n" +
                "    \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "\n" +
                "  }\n" +
                "\n" +
                "}\n";

        try{

            when(request.attribute("waltz-user")).thenReturn("testUser");
            when(request.bodyAsBytes()).thenReturn(requestBody.getBytes());

            ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);

            ProposedFlowCommandResponse proposedFlowCommandResponse = ImmutableProposedFlowCommandResponse.builder()
                    .message("SUCCESS")
                    .outcome("SUCCESS")
                    .proposedFlowCommand(command)
                    .proposedFlowId(1L)
                    .workflowDefinitionId(1L)
                    .build();
            when(makerCheckerService.proposeNewFlow(any(),any(),any())).thenReturn(proposedFlowCommandResponse);
            ProposedFlowCommandResponse result = makerCheckerEndpoint.proposeNewFlow(request,response);
            assertNotNull(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void testShouldReturnProposedFlowWhenServiceReturnsValue() throws IOException {
        long id = 123L;
       ProposedFlowDefinition proposedFlowDefExpected = new ProposedFlowDefinition();

        proposedFlowDefExpected.setLogicalFlowId(12345L);
        proposedFlowDefExpected.setPhysicalFlowId(12345L);
        proposedFlowDefExpected.setReasonCode(1234);

        when(request.queryParams("id")).thenReturn("123");
        when(makerCheckerService.getProposedFlowDefinitionById(id))
                .thenReturn(Optional.of(proposedFlowDefExpected));

        Optional<ProposedFlowDefinition> result =
                makerCheckerEndpoint.getProposedFlowDefinitionById(request, response);

        assertNotNull(result);
        assertTrue(result.isPresent());
        verify(makerCheckerService).getProposedFlowDefinitionById(id);
    }

    @Test
    void testShouldReturnEmptyWhenServiceReturnsEmpty() throws IOException {
        when(request.queryParams("id")).thenReturn("999");
        when(makerCheckerService.getProposedFlowDefinitionById(999L))
                .thenReturn(Optional.empty());

        Optional<ProposedFlowDefinition> result =
                makerCheckerEndpoint.getProposedFlowDefinitionById(request, response);

        assertTrue(!result.isPresent());
    }

    @Test
    void testShouldThrowNumberFormatExceptionWhenIdIsNotANumber() {
        when(request.queryParams("id")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> {
            makerCheckerEndpoint.getProposedFlowDefinitionById(request, response);
        });

        verifyNoInteractions(makerCheckerService);
    }

    @Test
    void testShouldRethrowIOExceptionFromServiceWhenDbIsDown() throws IOException {
        when(request.queryParams("id")).thenReturn("42");
        when(makerCheckerService.getProposedFlowDefinitionById(42L))
                .thenThrow(new IOException("db down"));

        IOException exception = assertThrows(IOException.class, () -> {
            makerCheckerEndpoint.getProposedFlowDefinitionById(request, response);
        });
        assertEquals("db down", exception.getMessage());
    }
}