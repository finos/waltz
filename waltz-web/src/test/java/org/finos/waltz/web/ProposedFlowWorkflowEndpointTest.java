package org.finos.waltz.web;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdSelectionOptions;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowState;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowTransition;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.service.maker_checker.ProposedFlowWorkflowService;
import org.finos.waltz.web.endpoints.api.ProposedFlowWorkflowEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityKind.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.command.CommandOutcome.SUCCESS;
import static org.finos.waltz.model.proposed_flow.ProposalType.CREATE;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposedFlowWorkflowEndpointTest {

    ProposedFlowWorkflowEndpoint proposedFlowWorkflowEndpoint;

    @Mock
    ProposedFlowWorkflowService proposedFlowWorkflowService;
    @Mock
    private Request request;
    @Mock
    private Response response;

    @BeforeEach
    public void setUp() {
        proposedFlowWorkflowEndpoint = new ProposedFlowWorkflowEndpoint(proposedFlowWorkflowService);
    }

    @Test
    void testEndpoint() throws IOException {

        //Given
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
                "    \"name\": \"XYZ\",\n" +
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
                "    \"proposalType\": \"CREATE\",\n" +
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
                "    \"description\": \"App Description\",\n" +
                "\n" +
                "    \"entityLifecycleStatus\": \"ACTIVE\"\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  \"target\": {\n" +
                "\n" +
                "    \"description\": \"App Description\",\n" +
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

        //When
        when(request.attribute("waltz-user")).thenReturn("testUser");
        when(request.bodyAsBytes()).thenReturn(requestBody.getBytes());

        ProposedFlowCommand command = getJsonMapper().readValue(requestBody, ProposedFlowCommand.class);

        ProposedFlowCommandResponse proposedFlowCommandResponse = ImmutableProposedFlowCommandResponse.builder()
                .message(SUCCESS.name())
                .outcome(SUCCESS)
                .proposedFlowCommand(command)
                .proposedFlowId(1L)
                .workflowDefinitionId(1L)
                .build();
        when(proposedFlowWorkflowService.proposeNewFlow(any(), any())).thenReturn(proposedFlowCommandResponse);
        ProposedFlowCommandResponse result = proposedFlowWorkflowEndpoint.proposeNewFlow(request, response);

        //Then
        assertNotNull(result);
    }

    @Test
    void testShouldReturnProposedFlowWhenExists() {

        //Given
        long id = 42L;

        EntityReference entityReference = mkRef(APPLICATION, 33L, "test");

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

        EntityReference owningEntity = mkRef(APPLICATION, 18703L, "AMG", "Testing", "60487-1");

        PhysicalSpecification physicalSpecification = ImmutablePhysicalSpecification.builder()
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .id(567)
                .build();
        EntityReference proposedFlowEntity = mkRef(PROPOSED_FLOW, 1L, PROPOSED_FLOW.prettyName(), "Testing", "487-1");

        EntityWorkflowState workflowState = ImmutableEntityWorkflowState.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .state(PENDING_APPROVALS.name())
                .build();
        EntityWorkflowTransition workflowTransition = ImmutableEntityWorkflowTransition.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .fromState(PROPOSED_CREATE.name())
                .toState(PENDING_APPROVALS.name())
                .reason("flow proposed")
                .build();
        EntityWorkflowTransition workflowTransition2 = ImmutableEntityWorkflowTransition.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .fromState(PENDING_APPROVALS.name())
                .toState(SOURCE_APPROVED.name())
                .reason("source approved")
                .build();
        List<EntityWorkflowTransition> workflowTransitionList = new ArrayList<>();
        workflowTransitionList.add(workflowTransition);
        workflowTransitionList.add(workflowTransition2);
        ProposedFlowCommand proposedFlowcommand = ImmutableProposedFlowCommand.builder()
                .source(entityReference)
                .target(entityReference)
                .reason(reason)
                .logicalFlowId(123L)
                .physicalFlowId(234L)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(CREATE)
                .build();

        ProposedFlowResponse expected = ImmutableProposedFlowResponse.builder()
                .id(1L)
                .sourceEntity(mkRef(APPLICATION, 601L))
                .targetEntity(mkRef(APPLICATION, 602L))
                .createdAt(LocalDateTime.now())
                .createdBy("anonymous")
                .flowDef(proposedFlowcommand)
                .workflowState(workflowState)
                .workflowTransitionList(workflowTransitionList)
                .proposalType(CREATE)
                .build();

        //When
        when(request.params("id")).thenReturn("42");
        when(proposedFlowWorkflowService.getProposedFlowResponseById(id)).thenReturn(expected);

        ProposedFlowResponse actual = proposedFlowWorkflowEndpoint.getProposedFlowById(request, response);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void getProposedFlows() throws IOException {

        //Given
        long id = 42L;

        EntityReference entityReference = mkRef(APPLICATION, 33L, "test");

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

        EntityReference owningEntity = mkRef(APPLICATION, 18703L, "AMG", "Testing", "60487-1");

        PhysicalSpecification physicalSpecification = ImmutablePhysicalSpecification.builder()
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .id(567)
                .build();
        EntityReference proposedFlowEntity = mkRef(PROPOSED_FLOW, 1L, PROPOSED_FLOW.prettyName(), "Testing", "487-1");

        EntityWorkflowState workflowState = ImmutableEntityWorkflowState.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .state(PENDING_APPROVALS.name())
                .build();
        EntityWorkflowTransition workflowTransition = ImmutableEntityWorkflowTransition.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .fromState("PROPOSED_CREATE")
                .toState(PENDING_APPROVALS.name())
                .reason("flow proposed")
                .build();
        EntityWorkflowTransition workflowTransition2 = ImmutableEntityWorkflowTransition.builder()
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy("xyz@pqr.com")
                .provenance("waltz")
                .workflowId(7L)
                .entityReference(proposedFlowEntity)
                .fromState(PENDING_APPROVALS.name())
                .toState(SOURCE_APPROVED.name())
                .reason("source approved")
                .build();
        List<EntityWorkflowTransition> workflowTransitionList = new ArrayList<>();
        workflowTransitionList.add(workflowTransition);
        workflowTransitionList.add(workflowTransition2);
        ProposedFlowCommand proposedFlowcommand = ImmutableProposedFlowCommand.builder()
                .source(entityReference)
                .target(entityReference)
                .reason(reason)
                .logicalFlowId(123L)
                .physicalFlowId(234L)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(CREATE)
                .build();

        ProposedFlowResponse expected = ImmutableProposedFlowResponse.builder()
                .id(1L)
                .sourceEntity(mkRef(APPLICATION, 601L))
                .targetEntity(mkRef(APPLICATION, 602L))
                .createdAt(LocalDateTime.now())
                .createdBy("anonymous")
                .flowDef(proposedFlowcommand)
                .workflowState(workflowState)
                .workflowTransitionList(workflowTransitionList)
                .proposalType(CREATE)
                .build();
        List<ProposedFlowResponse> expectedList = new ArrayList<>();
        expectedList.add(expected);
        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(mkRef(PERSON, 3025761))
                .scope(CHILDREN)
                .build();

        String requestBody = "{\n" +

                "  \"entityReference\": {\n" +
                "    \"id\": 3025761,\n" +
                "    \"kind\": \"PERSON\"\n" +
                "  },\n" +
                "  \"filters\": {},\n" +
                "  \"scope\": \"CHILDREN\"\n" +
                "}";
        //When
        when(request.bodyAsBytes()).thenReturn(requestBody.getBytes());
        when(proposedFlowWorkflowService.getProposedFlows(options)).thenReturn(expectedList);

        List<ProposedFlowResponse> actual = proposedFlowWorkflowEndpoint.findProposedFlows(request, response);

        //Then
        assertEquals(expectedList, actual);
        assertTrue(expectedList.size() > 0);
    }


    /* ----------Missing id Parameter ---------- */
    @Test
    public void testShouldThrowWhenIdParameterMissing() {

        //When
        when(request.params("id")).thenThrow(new IllegalArgumentException("Missing id"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> proposedFlowWorkflowEndpoint.getProposedFlowById(request, response)
        );

        //Then
        assertEquals("Missing id", ex.getMessage());
    }

    /* ----------Non-numeric id Parameter ---------- */
    @Test
    void testShouldThrowWhenIdIsNotNumeric() {

        //When
        when(request.params("id")).thenThrow(new NumberFormatException("For input string: \"abc\""));
        NumberFormatException ex = assertThrows(
                NumberFormatException.class,
                () -> proposedFlowWorkflowEndpoint.getProposedFlowById(request, response)
        );

        //Then
        assertTrue(ex.getMessage().contains("For input string: \"abc\""));
    }

    /* ----------Service Returns null ---------- */
    @Test
    void testShouldReturnNullWhenServiceReturnsNull() {

        //Given
        long id = 99L;

        //When
        when(request.params("id")).thenReturn("99");
        when(proposedFlowWorkflowService.getProposedFlowResponseById(id)).thenReturn(null);

        ProposedFlowResponse result = proposedFlowWorkflowEndpoint.getProposedFlowById(request, response);

        //Then
        assertNull(result);
    }

    /* ----------Service Throws NoSuchElementException ---------- */
    @Test
    void testShouldBubbleUpServiceException() {

        //Given
        long id = 77L;

        //When
        when(request.params("id")).thenReturn("77");
        when(proposedFlowWorkflowService.getProposedFlowResponseById(id))
                .thenThrow(new NoSuchElementException("ProposedFlow not found: 77"));

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> proposedFlowWorkflowEndpoint.getProposedFlowById(request, response));

        //Then
        assertEquals("ProposedFlow not found: 77", ex.getMessage());
    }
}