package org.finos.waltz.data.proposed_flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_STATE;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_TRANSITION;
import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;

@Repository
public class ProposedFlowDao {
    public static ProposedFlowRecord TO_DOMAIN_MAPPER(Record record) {
        ProposedFlowRecord proposedFlowRecord = new ProposedFlowRecord();
        proposedFlowRecord.setId(record.get(PROPOSED_FLOW.ID));
        proposedFlowRecord.setSourceEntityId(record.get(PROPOSED_FLOW.SOURCE_ENTITY_ID));
        proposedFlowRecord.setSourceEntityKind(record.get(PROPOSED_FLOW.SOURCE_ENTITY_KIND));
        proposedFlowRecord.setTargetEntityId(record.get(PROPOSED_FLOW.TARGET_ENTITY_ID));
        proposedFlowRecord.setTargetEntityKind(record.get(PROPOSED_FLOW.TARGET_ENTITY_KIND));
        proposedFlowRecord.setCreatedAt(record.get(PROPOSED_FLOW.CREATED_AT));
        proposedFlowRecord.setCreatedBy(record.get(PROPOSED_FLOW.CREATED_BY));
        proposedFlowRecord.setFlowDef(record.get(PROPOSED_FLOW.FLOW_DEF));
        proposedFlowRecord.setProposalType(record.get(PROPOSED_FLOW.PROPOSAL_TYPE));

        return proposedFlowRecord;
    }

    private final DSLContext dsl;

    @Autowired
    public ProposedFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
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

    /**
     * Fetches the single ProposedFlow row whose primary-key equals the given id.
     *
     * @param id primary key of the row (e.g. 1)
     * @return ProposedFlowRecord and null if no record found for the given id
     */

    public ProposedFlowRecord getProposedFlowById(long id) {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .from(PROPOSED_FLOW)
                .where(PROPOSED_FLOW.ID.eq(id))
                .fetchOneInto(ProposedFlowRecord.class);
    }


    public Result<Record> getProposedFlowsBySelector(Select<Record1<Long>> flowIdSelector, Long workflowId) throws JsonProcessingException {

        return dsl
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
    }
}


