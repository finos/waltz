package org.finos.waltz.data.proposed_flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.*;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.*;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_STATE;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_TRANSITION;
import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;

@Repository
public class ProposedFlowDao {
    public static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowDao.class);

    private final DSLContext dsl;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;
    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;

    @Autowired
    public ProposedFlowDao(DSLContext dsl, EntityWorkflowStateDao entityWorkflowStateDao, EntityWorkflowTransitionDao entityWorkflowTransitionDao, EntityWorkflowDefinitionDao entityWorkflowDefinitionDao) {
        checkNotNull(dsl, "dsl cannot be null");

        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.entityWorkflowDefinitionDao = entityWorkflowDefinitionDao;
        this.dsl = dsl;
    }

    public Long saveProposedFlow(String username, ProposedFlowCommand proposedFlowCommand) throws JsonProcessingException {
        ProposedFlowRecord proposedFlowRecord = dsl.newRecord(PROPOSED_FLOW);
        proposedFlowRecord.setFlowDef(getJsonMapper().writeValueAsString(proposedFlowCommand));
        proposedFlowRecord.setCreatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        proposedFlowRecord.setCreatedBy(username);
        proposedFlowRecord.setSourceEntityId(proposedFlowCommand.source().id());
        proposedFlowRecord.setSourceEntityKind(proposedFlowCommand.source().kind().name());
        proposedFlowRecord.setTargetEntityId(proposedFlowCommand.target().id());
        proposedFlowRecord.setTargetEntityKind(proposedFlowCommand.target().kind().name());
        proposedFlowRecord.setProposalType(proposedFlowCommand.proposalType().name());
        proposedFlowRecord.store();
        return proposedFlowRecord.getId();
    }

    public ProposedFlowResponse getProposedFlowResponseById(long id) {
        ProposedFlowRecord proposedFlowRecord = getProposedFlowById(id);
        checkNotNull(proposedFlowRecord, format("ProposedFlow not found: %d", proposedFlowRecord.getId()));

        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowRecord.getId());
        EntityWorkflowView entityWorkflowView = getEntityWorkflowView(PROPOSE_FLOW_LIFECYCLE_WORKFLOW, entityReference);
        try {
            ProposedFlowCommand flowDefinition = getJsonMapper().readValue(proposedFlowRecord.getFlowDef(), ProposedFlowCommand.class);

            return ImmutableProposedFlowResponse.builder()
                    .id(proposedFlowRecord.getId())
                    .createdAt(proposedFlowRecord.getCreatedAt().toLocalDateTime())
                    .createdBy(proposedFlowRecord.getCreatedBy())
                    .flowDef(flowDefinition)
                    .workflowState(entityWorkflowView.workflowState())
                    .workflowTransitionList(entityWorkflowView.workflowTransitionList())
                    .build();

        } catch (JsonProcessingException e) {
            LOG.error("Invalid flow definition JSON : {} ", e.getMessage());
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }
    }

    public EntityWorkflowView getEntityWorkflowView(String workFlowDefName, EntityReference ref) {
        checkNotNull(workFlowDefName, "workFlowDefName cannot be null");
        checkNotNull(ref, "ref cannot be null");

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(workFlowDefName);
        Long workFlowId = Optional.ofNullable(entityWorkflowDefinition)
                .flatMap(EntityWorkflowDefinition::id)
                .orElseThrow(() -> new NoSuchElementException("Workflow not found"));
        EntityWorkflowState entityWorkflowState = entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(workFlowId, ref);
        List<EntityWorkflowTransition> entityWorkflowTransitionList = entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workFlowId, ref);

        return ImmutableEntityWorkflowView.builder()
                .workflowDefinition(entityWorkflowDefinition)
                .workflowState(entityWorkflowState)
                .workflowTransitionList(entityWorkflowTransitionList)
                .build();
    }

    public List<ProposedFlowResponse> getProposedFlowsBySelector(Select<Record1<Long>> flowIdSelector, Long workflowId) throws JsonProcessingException {
        Result<Record> flatResults = dsl
                .select(PROPOSED_FLOW.fields())
                .select(ENTITY_WORKFLOW_STATE.fields())
                .select(ENTITY_WORKFLOW_TRANSITION.fields())
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId)).and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .join(ENTITY_WORKFLOW_TRANSITION)
                .on(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                .and(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId)).and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .where(PROPOSED_FLOW.ID.in(flowIdSelector))
                .fetch();

        // 1. Group the flat records by the proposed flow ID.
        //    The result is a Map where the key is the flow ID and the value is a list of all records for that flow.
        Map<Long, List<Record>> recordsByFlowId = flatResults.stream()
                .collect(Collectors.groupingBy(r -> r.get(Tables.PROPOSED_FLOW.ID)));

        // 2. Process each group to create a single, consolidated ProposedFlowResponse.
        return recordsByFlowId
                .values()
                .stream()
                .map(recordsForOneFlow -> {
                    // All records in this list share the same proposed flow and workflow state.
                    // We can safely take the first record to get this common information.
                    Record firstRecord = recordsForOneFlow.get(0);

                    // Extract the common objects.
                    ProposedFlowRecord proposedFlowRecord = firstRecord.into(ProposedFlowRecord.class);
                    EntityWorkflowState entityWorkflowState = EntityWorkflowStateDao.TO_DOMAIN_MAPPER.map(firstRecord);

                    // Now, iterate over all records in the group to build the complete list of transitions.
                    List<EntityWorkflowTransition> transitions = recordsForOneFlow.stream()
                            .map(r -> EntityWorkflowTransitionDao.TO_DOMAIN_MAPPER.map(r))
                            .collect(Collectors.toList());

                    // Deserialize the flow definition from the JSON string.
                    try {
                        ProposedFlowCommand flowDefinition = getJsonMapper()
                                .readValue(proposedFlowRecord.getFlowDef(), ProposedFlowCommand.class);

                        // 3. Build the final response object with the complete list of transitions.
                        return createProposedFlowView(
                                flowDefinition,
                                transitions,
                                proposedFlowRecord,
                                entityWorkflowState);
                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to parse flow definition JSON for proposed flow id: " + proposedFlowRecord.getId(), e);
                        throw new IllegalStateException(
                                "Failed to parse flow definition JSON for proposed flow id: " + proposedFlowRecord.getId(), e);

                    }
                })
                .collect(Collectors.toList());
    }

    private ProposedFlowResponse createProposedFlowView(ProposedFlowCommand flowDefinition, List<EntityWorkflowTransition> updatedTransitions, ProposedFlowRecord proposedFlowRecord, EntityWorkflowState entityWorkflowState) {
        return ImmutableProposedFlowResponse.builder()
                .id(proposedFlowRecord.getId())
                .createdAt(proposedFlowRecord.getCreatedAt().toLocalDateTime())
                .createdBy(proposedFlowRecord.getCreatedBy())
                .flowDef(flowDefinition)
                .workflowState(entityWorkflowState)
                .workflowTransitionList(updatedTransitions)
                .build();
    }

    private ProposedFlowRecord getProposedFlowById(long id) {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .from(PROPOSED_FLOW)
                .where(PROPOSED_FLOW.ID.eq(id))
                .fetchOneInto(ProposedFlowRecord.class);
    }
}


