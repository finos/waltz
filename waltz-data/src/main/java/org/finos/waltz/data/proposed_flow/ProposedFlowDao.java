package org.finos.waltz.data.proposed_flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowView;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.proposed_flow.ApproverWithType;
import org.finos.waltz.model.proposed_flow.ImmutableApproverWithType;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposalType;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.ProposedFlow;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectUnionStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static org.finos.waltz.model.EntityKind.PHYSICAL_FLOW;
import static org.finos.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.Operation.*;
import static org.finos.waltz.model.proposed_flow.ProposalType.*;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.END_STATES;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.TARGET_APPROVED;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_STATE;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_TRANSITION;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.InvolvementGroupEntry.INVOLVEMENT_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

@Repository
public class ProposedFlowDao {

    public static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowDao.class);
    private static final List<String> ACTION_PENDING_SOURCE_APPROVER_STATE = Arrays.asList(
            ProposedFlowWorkflowState.PENDING_APPROVALS.name(),
            ProposedFlowWorkflowState.TARGET_APPROVED.name());
    private static final List<String> ACTION_PENDING_TARGET_APPROVER_STATE = Arrays.asList(
            ProposedFlowWorkflowState.PENDING_APPROVALS.name(),
            ProposedFlowWorkflowState.SOURCE_APPROVED.name());
    // Mapper to convert a DB record into our ApproverWithType object
    private static final RecordMapper<Record, ApproverWithType> TO_APPROVER_WITH_TYPE_MAPPER = r -> {
        Person person = PersonDao.personMapper.map(r);
        String approverType = r.get("approver_type", String.class);
        Long involvementKindId = r.get(INVOLVEMENT_KIND.ID);
        String involvementKindName = r.get(INVOLVEMENT_KIND.NAME);

        return ImmutableApproverWithType.builder()
                .person(person)
                .approverType(approverType)
                .involvementKindId(involvementKindId)
                .involvementKindName(involvementKindName)
                .build();
    };

    private final DSLContext dsl;
    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;

    @Autowired
    public ProposedFlowDao(DSLContext dsl, EntityWorkflowStateDao entityWorkflowStateDao, EntityWorkflowTransitionDao entityWorkflowTransitionDao, EntityWorkflowDefinitionDao entityWorkflowDefinitionDao) {
        checkNotNull(dsl, "dsl cannot be null");

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
        proposedFlowCommand.logicalFlowId().ifPresent(proposedFlowRecord::setLogicalFlowId);
        proposedFlowCommand.physicalFlowId().ifPresent(proposedFlowRecord::setPhysicalFlowId);
        proposedFlowCommand.specification().id().ifPresent(proposedFlowRecord::setSpecificationId);
        proposedFlowRecord.store();
        return proposedFlowRecord.getId();
    }

    public ProposedFlowResponse getProposedFlowResponseById(long id) {
        ProposedFlowRecord proposedFlowRecord = getProposedFlowById(id);
        checkNotNull(proposedFlowRecord, format("ProposedFlow not found: %d", proposedFlowRecord.getId()));

        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowRecord.getId());
        EntityWorkflowView entityWorkflowView = entityWorkflowDefinitionDao.getEntityWorkflowView(PROPOSE_FLOW_LIFECYCLE_WORKFLOW, entityReference);
        try {
            ProposedFlowCommand flowDefinition = getJsonMapper().readValue(proposedFlowRecord.getFlowDef(), ProposedFlowCommand.class);

            return ImmutableProposedFlowResponse.builder()
                    .id(proposedFlowRecord.getId())
                    .createdAt(proposedFlowRecord.getCreatedAt().toLocalDateTime())
                    .createdBy(proposedFlowRecord.getCreatedBy())
                    .flowDef(flowDefinition)
                    .workflowState(entityWorkflowView.workflowState())
                    .workflowTransitionList(entityWorkflowView.workflowTransitionList())
                    .logicalFlowId(entityWorkflowView.entityWorkflowResultList()
                            .stream()
                            .filter(e -> e.kind().equals(LOGICAL_DATA_FLOW))
                            .findFirst()
                            .map(EntityReference::id).orElse(proposedFlowRecord.getLogicalFlowId()))
                    .physicalFlowId(entityWorkflowView.entityWorkflowResultList()
                            .stream()
                            .filter(e -> e.kind().equals(PHYSICAL_FLOW))
                            .findFirst()
                            .map(EntityReference::id).orElse(proposedFlowRecord.getPhysicalFlowId()))
                    .specificationId(entityWorkflowView.entityWorkflowResultList()
                            .stream()
                            .filter(e -> e.kind().equals(PHYSICAL_SPECIFICATION))
                            .findFirst()
                            .map(EntityReference::id).orElse(proposedFlowRecord.getSpecificationId()))
                    .build();

        } catch (JsonProcessingException e) {
            LOG.error("Invalid flow definition JSON : {} ", e.getMessage());
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }
    }

    public List<ProposedFlowResponse> getProposedFlowsForTimeout(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Long workflowId = fetchWorkflowID();

        SelectFieldOrAsterisk[] selectFields = Stream
                .of(PROPOSED_FLOW.fields(), ENTITY_WORKFLOW_STATE.fields())
                .flatMap(Arrays::stream)
                .map(f -> (SelectFieldOrAsterisk) f)
                .toArray(SelectFieldOrAsterisk[]::new);

        Result<Record> results = dsl
                .select(selectFields)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .where(PROPOSED_FLOW.ID.in(ids))
                .fetch();

        return results.stream()
                .map(record -> {
                    try {
                        ProposedFlowRecord proposedFlowRecord = record.into(ProposedFlowRecord.class);
                        ProposedFlowCommand flowDefinition = getJsonMapper().readValue(proposedFlowRecord.getFlowDef(), ProposedFlowCommand.class);

                        return ImmutableProposedFlowResponse.builder()
                                .id(record.get(PROPOSED_FLOW.ID))
                                .createdAt(record.get(PROPOSED_FLOW.CREATED_AT).toLocalDateTime())
                                .createdBy(record.get(PROPOSED_FLOW.CREATED_BY))
                                .flowDef(flowDefinition)
                                .workflowState(EntityWorkflowStateDao.TO_DOMAIN_MAPPER.map(record))
                                .build();
                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to parse flow definition JSON for proposed flow id: {}",
                                record.get(PROPOSED_FLOW.ID), e);
                        throw new IllegalStateException(
                                "Failed to parse flow definition JSON for proposed flow id: " + record.get(PROPOSED_FLOW.ID), e);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<ProposedFlowResponse> getProposedFlowsByUser(Long personId, boolean isHierarchyRequired, Long workflowId) throws JsonProcessingException {

        Result<Record> flatResults = fetchFlowsForPerson(personId, isHierarchyRequired, workflowId);
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

    private Result<Record> fetchFlowsForPerson(Long personId,
                                               boolean includeSubordinates,
                                               Long workflowId) {
        SelectFieldOrAsterisk[] selectFields = Stream.of(PROPOSED_FLOW.fields(), ENTITY_WORKFLOW_STATE.fields(), ENTITY_WORKFLOW_TRANSITION.fields())
                .flatMap(Arrays::stream)
                .map(f -> (SelectFieldOrAsterisk) f)
                .toArray(SelectFieldOrAsterisk[]::new);

        // CTE to define the scope of people (person and, optionally, their subordinates)
        CommonTableExpression<Record3<Long, String, String>> personScopeCte = getPersonScope(includeSubordinates, getCurrentPerson(personId));
        Table<Record3<Long, String, String>> personScopeTbl = personScopeCte.as("ps");

        // CTE to find all permissions for the people in scope related to proposed flows
        CommonTableExpression<Record3<String, Long, String>> userPermissionsCte = name("userPermissions")
                .fields("entity_kind", "entity_id", "operation")
                .as(select(
                        INVOLVEMENT.ENTITY_KIND,
                        INVOLVEMENT.ENTITY_ID,
                        PERMISSION_GROUP_INVOLVEMENT.OPERATION)
                        .from(personScopeTbl)
                        .join(PERSON).on(personScopeTbl.field("employee_id", String.class).eq(PERSON.EMPLOYEE_ID))
                        .join(INVOLVEMENT).on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                        .join(INVOLVEMENT_KIND).on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                        .join(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID))
                        .join(PERMISSION_GROUP_INVOLVEMENT).on(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID.eq(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID))
                        .where(PERSON.IS_REMOVED.isFalse())
                        .and(INVOLVEMENT.ENTITY_KIND.eq(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND))
                        .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(EntityKind.PROPOSED_FLOW.name())));

        Table<?> userPermissions = userPermissionsCte.as("userPermissions");

        SelectConditionStep<Record> queryByCreator = dsl
                .select(selectFields)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID)
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .join(ENTITY_WORKFLOW_TRANSITION)
                .on(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(PROPOSED_FLOW.ID)
                        .and(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .where(PROPOSED_FLOW.CREATED_BY.in(
                        select(personScopeTbl.field("email", String.class))
                                .from(personScopeTbl)
                ));

        return dsl
                .with(personScopeCte)
                .with(userPermissionsCte)
                .select(selectFields)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID)
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .join(ENTITY_WORKFLOW_TRANSITION)
                .on(ENTITY_WORKFLOW_TRANSITION.ENTITY_ID.eq(PROPOSED_FLOW.ID)
                        .and(ENTITY_WORKFLOW_TRANSITION.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_TRANSITION.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .where(exists(
                        selectOne()
                                .from(userPermissions)
                                .where(userPermissions.field("entity_kind", String.class).eq(PROPOSED_FLOW.SOURCE_ENTITY_KIND)
                                        .and(userPermissions.field("entity_id", Long.class).eq(PROPOSED_FLOW.SOURCE_ENTITY_ID)))
                                .or(userPermissions.field("entity_kind", String.class).eq(PROPOSED_FLOW.TARGET_ENTITY_KIND)
                                        .and(userPermissions.field("entity_id", Long.class).eq(PROPOSED_FLOW.TARGET_ENTITY_ID)))
                ))
                .union(queryByCreator)
                .fetch();
    }

    private SelectConditionStep<Record3<Long, String, String>> getCurrentPerson(Long personId) {
        return dsl
                .select(PERSON.ID, PERSON.EMPLOYEE_ID, PERSON.EMAIL)
                .from(PERSON)
                .where(PERSON.ID.eq(personId));
    }

    private CommonTableExpression<Record3<Long, String, String>> getPersonScope(boolean includeSubordinates, SelectConditionStep<Record3<Long, String, String>> currentPerson) {

        return includeSubordinates ?
                name("person_scope").fields("id", "employee_id", "email").as(getPersonWithSubordinates(currentPerson))
                : name("person_scope").fields("id", "employee_id", "email").as(currentPerson);

    }

    private SelectUnionStep<Record3<Long, String, String>> getPersonWithSubordinates(SelectConditionStep<Record3<Long, String, String>> currentPerson) {
        return dsl
                .select(PERSON.ID, PERSON.EMPLOYEE_ID, PERSON.EMAIL)
                .from(PERSON)
                .join(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .join(currentPerson.asTable("cp"))
                .on(PERSON_HIERARCHY.MANAGER_ID.eq(field(name("cp", "employee_id"), String.class)))
                .where(PERSON.IS_REMOVED.eq(false))
                .union(
                        dsl
                                .select(field(name("id"), Long.class),
                                        field(name("employee_id"), String.class),
                                        field(name("email"), String.class))
                                .from(currentPerson));
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

    public List<ProposedFlowRecord> proposedFlowRecordsByProposalType(ProposedFlowCommand proposedFlowCommand) {
        Long workflowId = fetchWorkflowID();
        Condition proposalTypeCondition = getProposalTypeCondition(proposedFlowCommand.proposalType());

        List<ProposedFlowRecord> records =
                dsl
                        .selectDistinct(PROPOSED_FLOW.fields())
                        .from(PROPOSED_FLOW)
                        .join(ENTITY_WORKFLOW_STATE)
                        .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId)).and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                        .where(PROPOSED_FLOW.SOURCE_ENTITY_ID.eq(proposedFlowCommand.source().id()))
                        .and(PROPOSED_FLOW.SOURCE_ENTITY_KIND.eq(proposedFlowCommand.source().kind().name()))
                        .and(PROPOSED_FLOW.TARGET_ENTITY_ID.eq(proposedFlowCommand.target().id()))
                        .and(PROPOSED_FLOW.TARGET_ENTITY_KIND.eq(proposedFlowCommand.target().kind().name()))
                        .and(proposalTypeCondition)
                        .and(ENTITY_WORKFLOW_STATE.STATE.notIn(END_STATES))
                        .fetchInto(ProposedFlowRecord.class);
        return records;
    }

    // Checks if the app is a source/target for any flow where the current user is an approver
    public boolean isAppInvolvedInPendingApprovals(EntityReference appRef, String username, Long workflowId) {
        // CTE to find the current user's employee_id from their username (email)
        var personEmployeeId = select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.EMAIL.eq(username)
                        .and(PERSON.IS_REMOVED.isFalse()));

        // CTE to find all specific permissions for that user related to proposed flows
        CommonTableExpression<?> userPermissionsCte = name("userPermissions")
                .fields("entity_kind", "entity_id", "operation")
                .as(dsl.select(
                                INVOLVEMENT.ENTITY_KIND,
                                INVOLVEMENT.ENTITY_ID,
                                PERMISSION_GROUP_INVOLVEMENT.OPERATION)
                        .from(PERMISSION_GROUP_INVOLVEMENT)
                        .join(INVOLVEMENT_GROUP_ENTRY)
                        .on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                        .join(INVOLVEMENT)
                        .on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID))
                        .where(INVOLVEMENT.EMPLOYEE_ID.eq(personEmployeeId)) // Filter involvements for the current user
                        .and(INVOLVEMENT.ENTITY_KIND.eq(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND))
                        .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(EntityKind.PROPOSED_FLOW.name())));

        // Condition to check if the app is the target of the flow
        Condition appIsTarget = PROPOSED_FLOW.TARGET_ENTITY_ID.eq(appRef.id())
                        .and(PROPOSED_FLOW.TARGET_ENTITY_KIND.eq(appRef.kind().name()));

        // Create a table reference to the CTE to be used in the main query
        Table<?> userPermissions = userPermissionsCte.as("userPermissions");

        // Condition to check if the user has APPROVE/REJECT permissions on the flow's source or target
        Condition userIsApprover = exists(
                selectOne()
                        .from(userPermissions)
                        .where(userPermissions.field("operation", String.class).in("APPROVE", "REJECT"))
                        .and(
                                userPermissions.field("entity_kind", String.class).eq(PROPOSED_FLOW.TARGET_ENTITY_KIND)
                                        .and(userPermissions.field("entity_id", Long.class).eq(PROPOSED_FLOW.TARGET_ENTITY_ID))
                        ));


        List<ProposedFlowWorkflowState> endStatesAndTagetApproved = new ArrayList<>(END_STATES);
        endStatesAndTagetApproved.add(TARGET_APPROVED);

        // Build the final query
        Integer count = dsl
                .with(userPermissionsCte)
                .selectCount()
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE).on(PROPOSED_FLOW.ID.eq(ENTITY_WORKFLOW_STATE.ENTITY_ID)
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .where(ENTITY_WORKFLOW_STATE.STATE.notIn(endStatesAndTagetApproved))
                .and(appIsTarget)
                .and(userIsApprover)
                .fetchOne(0, Integer.class);
        return count != null && count > 0;
    }

    // Checks if there are any pending flows of kind 'CREATE' for the given app
    public boolean hasPendingCreations(EntityReference appRef, Long workflowId) {
        Condition condition = PROPOSED_FLOW.SOURCE_ENTITY_ID.eq(appRef.id()).and(PROPOSED_FLOW.SOURCE_ENTITY_KIND.eq(appRef.kind().name()))
                .or(PROPOSED_FLOW.TARGET_ENTITY_ID.eq(appRef.id()).and(PROPOSED_FLOW.TARGET_ENTITY_KIND.eq(appRef.kind().name())));

        Integer count = dsl
                .selectCount()
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE).on(PROPOSED_FLOW.ID.eq(ENTITY_WORKFLOW_STATE.ENTITY_ID)
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .where(PROPOSED_FLOW.PROPOSAL_TYPE.eq(ProposalType.CREATE.name()))
                .and(ENTITY_WORKFLOW_STATE.STATE.notIn(END_STATES))
                .and(condition)
                .fetchOne(0, Integer.class);
        return count != null && count > 0;
    }

    public Set<Long> findPhysicalFlowIdsInPendingProposals(Set<Long> logicalFlowIds, Long workflowId) {

        if (logicalFlowIds == null || logicalFlowIds.isEmpty()) {
            return Collections.emptySet();
        }

        return dsl
                .selectDistinct(PROPOSED_FLOW.PHYSICAL_FLOW_ID)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE).on(PROPOSED_FLOW.ID.eq(ENTITY_WORKFLOW_STATE.ENTITY_ID)
                        .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                        .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name())))
                .where(PROPOSED_FLOW.PROPOSAL_TYPE.in(
                        ProposalType.EDIT.name(),
                        ProposalType.DELETE.name()))
                .and(ENTITY_WORKFLOW_STATE.STATE.notIn(END_STATES))
                .and(PROPOSED_FLOW.LOGICAL_FLOW_ID.in(logicalFlowIds))
                .and(PROPOSED_FLOW.PHYSICAL_FLOW_ID.isNotNull())
                .fetchSet(PROPOSED_FLOW.PHYSICAL_FLOW_ID);
    }

    private Condition getProposalTypeCondition(ProposalType proposalType) {
        Condition proposalTypeCondition;
        if (proposalType == CREATE)
            proposalTypeCondition = PROPOSED_FLOW.PROPOSAL_TYPE.eq(CREATE.name());
        else if (proposalType == EDIT || proposalType == DELETE)
            proposalTypeCondition = PROPOSED_FLOW.PROPOSAL_TYPE.in(EDIT.name(), DELETE.name());
        else
            throw new IllegalArgumentException("Unexpected proposal type: " + proposalType);
        return proposalTypeCondition;
    }

    private Long fetchWorkflowID() {
        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        return Optional.ofNullable(entityWorkflowDefinition)
                .flatMap(EntityWorkflowDefinition::id)
                .orElseThrow(() -> new NoSuchElementException("Workflow not found"));
    }

    public List<Long> fetchPendingActionFlowsForPersonWhereSourceOrTargetApprover(Long personId) {
        Long workflowId = fetchWorkflowID();
        TableField<ProposedFlowRecord, String> targetEntityKind = PROPOSED_FLOW.TARGET_ENTITY_KIND;
        TableField<ProposedFlowRecord, String> sourceEntityKind = PROPOSED_FLOW.SOURCE_ENTITY_KIND;
        TableField<ProposedFlowRecord, Long> targetEntityId = PROPOSED_FLOW.TARGET_ENTITY_ID;
        TableField<ProposedFlowRecord, Long> sourceEntityId = PROPOSED_FLOW.SOURCE_ENTITY_ID;

        List<Long> pendingActionsWhenSource = mkWorkflowQueryForState(dsl, personId, workflowId, ACTION_PENDING_SOURCE_APPROVER_STATE, sourceEntityKind, sourceEntityId);
        List<Long> pendingActionsWhenTarget = mkWorkflowQueryForState(dsl, personId, workflowId, ACTION_PENDING_TARGET_APPROVER_STATE, targetEntityKind, targetEntityId);

        return Stream.concat(pendingActionsWhenSource.stream(), pendingActionsWhenTarget.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public long fetchCountPendingActionFlowsForPersonWhereSourceOrTargetApprover(Long personId) {
        return fetchPendingActionFlowsForPersonWhereSourceOrTargetApprover(personId).size();
    }

    private List<Long> mkWorkflowQueryForState(DSLContext tx, Long personId, Long workflowId, List<String> stateNames, TableField<ProposedFlowRecord, String> entityKindField, TableField<ProposedFlowRecord, Long> entityIdField) {

        // CTE to locate person and employee id for permission check
        CommonTableExpression<Record2<String, Long>> personCte = name("personCTE")
                .fields("employee_id", "id")
                .as(tx.select(PERSON.EMPLOYEE_ID, PERSON.ID)
                        .from(PERSON)
                        .where(PERSON.ID.eq(personId)));

        // CTE to find permission-based involvements for PROPOSED_FLOW
        CommonTableExpression<Record3<String, Long, String>> userPermissionsCte = name("userPermissions")
                .fields("entity_kind", "entity_id", "operation")
                .as(tx.select(INVOLVEMENT.ENTITY_KIND, INVOLVEMENT.ENTITY_ID, PERMISSION_GROUP_INVOLVEMENT.OPERATION)
                        .from(PERMISSION_GROUP_INVOLVEMENT)
                        .join(INVOLVEMENT_GROUP_ENTRY)
                        .on(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                        .join(INVOLVEMENT)
                        .on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID))
                        .join(PERSON)
                        .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                        .where(PERSON.IS_REMOVED.eq(false))
                        .and(PERSON.EMPLOYEE_ID.eq(select(field(name("employee_id"), String.class)).from(personCte)))
                        .and(INVOLVEMENT.ENTITY_KIND.eq(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND))
                        .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(EntityKind.PROPOSED_FLOW.name())));

        return tx
                .with(personCte)
                .with(userPermissionsCte)
                .selectDistinct(PROPOSED_FLOW.ID)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .and(ENTITY_WORKFLOW_STATE.STATE.in(stateNames))
                .join(userPermissionsCte)
                .on(userPermissionsCte.field("entity_kind", String.class).eq(entityKindField))
                .and(userPermissionsCte.field("entity_id", Long.class).eq(entityIdField))
                .and(userPermissionsCte.field("operation", String.class).in(APPROVE.name(), REJECT.name()))
                .fetch(ProposedFlow.PROPOSED_FLOW.ID);

    }

    public List<Long> findPendingFlowsOlderThanDays(int timeoutDays) {
        Long workflowId = fetchWorkflowID();

        Timestamp threshold = Timestamp.valueOf(DateTimeUtilities.nowUtc().minusDays(timeoutDays));

        return dsl
                .select(PROPOSED_FLOW.ID)
                .from(PROPOSED_FLOW)
                .join(ENTITY_WORKFLOW_STATE)
                .on(ENTITY_WORKFLOW_STATE.ENTITY_ID.eq(PROPOSED_FLOW.ID))
                .and(ENTITY_WORKFLOW_STATE.WORKFLOW_ID.eq(workflowId))
                .and(ENTITY_WORKFLOW_STATE.ENTITY_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .where(PROPOSED_FLOW.CREATED_AT.le(threshold))
                .and(ENTITY_WORKFLOW_STATE.STATE.notIn(END_STATES))
                .fetch(r -> r.get(PROPOSED_FLOW.ID));
    }

   /**
     * Finds all source and target approvers for a given proposed flow in a single query.
     *
     * @param proposedFlowId The ID of the proposed flow.
     * @return A single list containing all approvers, each tagged with their type ('SOURCE' or 'TARGET').
     */
    public List<ApproverWithType> findApproversForProposedFlow(long proposedFlowId) {

        Condition sourceJoinCondition = PROPOSED_FLOW.SOURCE_ENTITY_ID.eq(INVOLVEMENT.ENTITY_ID)
                .and(PROPOSED_FLOW.SOURCE_ENTITY_KIND.eq(INVOLVEMENT.ENTITY_KIND))
                .and(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND.eq(PROPOSED_FLOW.SOURCE_ENTITY_KIND));

        Condition targetJoinCondition = PROPOSED_FLOW.TARGET_ENTITY_ID.eq(INVOLVEMENT.ENTITY_ID)
                .and(PROPOSED_FLOW.TARGET_ENTITY_KIND.eq(INVOLVEMENT.ENTITY_KIND))
                .and(PERMISSION_GROUP_INVOLVEMENT.PARENT_KIND.eq(PROPOSED_FLOW.TARGET_ENTITY_KIND));

        Select<Record> sourceApprovers = mkApproverQuery(proposedFlowId, "SOURCE", sourceJoinCondition);
        Select<Record> targetApprovers = mkApproverQuery(proposedFlowId, "TARGET", targetJoinCondition);

        // Execute a single UNION query against the database
        return sourceApprovers
                .union(targetApprovers)
                .fetch(TO_APPROVER_WITH_TYPE_MAPPER);
    }


    // This private helper method remains mostly the same, but selects all person fields
    private Select<Record> mkApproverQuery(long proposedFlowId,
                                           String approverType,
                                           Condition joinCondition) {
        return dsl
                .select(val(approverType).as("approver_type"))
                .select(PERSON.fields()) // Select all fields from the person table
                .select(INVOLVEMENT_KIND.ID, INVOLVEMENT_KIND.NAME) // Select the involvements fields
                .from(PERSON)
                .join(INVOLVEMENT).on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .join(INVOLVEMENT_KIND).on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                .join(INVOLVEMENT_GROUP_ENTRY).on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID))
                .join(PERMISSION_GROUP_INVOLVEMENT).on(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID.eq(PERMISSION_GROUP_INVOLVEMENT.INVOLVEMENT_GROUP_ID))
                .join(PROPOSED_FLOW).on(joinCondition)
                .where(PROPOSED_FLOW.ID.eq(proposedFlowId))
                .and(PERMISSION_GROUP_INVOLVEMENT.SUBJECT_KIND.eq(EntityKind.PROPOSED_FLOW.name()))
                .and(PERMISSION_GROUP_INVOLVEMENT.OPERATION.in("APPROVE", "REJECT"));
    }
}
