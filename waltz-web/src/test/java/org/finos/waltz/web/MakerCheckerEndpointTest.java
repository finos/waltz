package org.finos.waltz.web;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.*;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void testShouldReturnProposedFlowWhenExists() {
        long id = 42L;

        EntityReference entityReference = ImmutableEntityReference.builder()
                .kind(APPLICATION)
                .id(33L)
                .name("test")
                .externalId("456-1")
                .build();

        Reason reason = ImmutableReason.builder()
                .description("test")
                .ratingId(3)
                .build();

        TransportKindValue transportKindValue = TransportKindValue.of("UNKNOWN");
        FrequencyKindValue frequencyKindValue = FrequencyKindValue.of("QUARTERLY");
        CriticalityValue criticalityValue = CriticalityValue.of("low");

        Set<Long> dataTypeIdSet = new HashSet<>();
        dataTypeIdSet.add(2222L);
        dataTypeIdSet.add(3333L);

        FlowAttributes flowAttributes = ImmutableFlowAttributes.builder()
                .name("sss")
                .transport(transportKindValue)
                .frequency(frequencyKindValue)
                .basisOffset(0)
                .criticality(criticalityValue)
                .description("testing")
                .externalId("567s")
                .build();

        EntityReference owningEntity = ImmutableEntityReference.builder()
                .id(18703L)
                .kind(APPLICATION)
                .name("AMG")
                .externalId("60487-1")
                .description("Testing")
                .build();

        PhysicalSpecification physicalSpecification = ImmutablePhysicalSpecification.builder()
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .id(567)
                .build();

        ProposedFlowCommand proposedFlowcommand = ImmutableProposedFlowCommand.builder()
                .source(entityReference)
                .target(entityReference)
                .reason(reason)
                .logicalFlowId(123L)
                .physicalFlowId(234L)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .build();

        ProposedFlowResponse expected = ImmutableProposedFlowResponse.builder()
                .id(1L)
                .sourceEntityId(601L)
                .sourceEntityKind("APPLICATION")
                .targetEntityId(602L)
                .targetEntityKind("APPLICATION")
                .createdAt(LocalDateTime.now())
                .createdBy("anonymous")
                .flowDef(proposedFlowcommand)
                .build();

        when(request.params("id")).thenReturn("42");
        when(makerCheckerService.getProposedFlowById(id)).thenReturn(expected);

        ProposedFlowResponse actual = makerCheckerEndpoint.getProposedFlowById(request, response);

        assertEquals(expected, actual);
    }

    /* ----------Missing id Parameter ---------- */
    @Test
    void testShouldThrowWhenIdParameterMissing() {
        when(request.params("id")).thenThrow(new IllegalArgumentException("Missing id"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> makerCheckerEndpoint.getProposedFlowById(request, response)
        );
        assertEquals("Missing id", ex.getMessage());
    }

    /* ----------Non-numeric id Parameter ---------- */
    @Test
    void testShouldThrowWhenIdIsNotNumeric() {
        when(request.params("id")).thenThrow(new NumberFormatException("For input string: \"abc\""));
        NumberFormatException ex = assertThrows(
                NumberFormatException.class,
                () -> makerCheckerEndpoint.getProposedFlowById(request, response)
        );
        assertTrue(ex.getMessage().contains("For input string: \"abc\""));
    }

    /* ----------Service Returns null ---------- */
    @Test
    void testShouldReturnNullWhenServiceReturnsNull() {
        long id = 99L;

        when(request.params("id")).thenReturn("99");
        when(makerCheckerService.getProposedFlowById(id)).thenReturn(null);

        ProposedFlowResponse result = makerCheckerEndpoint.getProposedFlowById(request, response);

        assertNull(result);
    }

    /* ----------Service Throws NoSuchElementException ---------- */
    @Test
    void testShouldBubbleUpServiceException() {
        long id = 77L;
        when(request.params("id")).thenReturn("77");
        when(makerCheckerService.getProposedFlowById(id))
                .thenThrow(new NoSuchElementException("ProposedFlow not found: 77"));

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> makerCheckerEndpoint.getProposedFlowById(request, response));

        assertEquals("ProposedFlow not found: 77", ex.getMessage());
    }
}