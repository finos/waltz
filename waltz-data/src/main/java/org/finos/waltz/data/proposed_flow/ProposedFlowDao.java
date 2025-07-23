package org.finos.waltz.data.proposed_flow;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class ProposedFlowDao {

    private final DSLContext dsl;


    @Autowired
    public ProposedFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public Long saveProposedFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand){
        ProposedFlowRecord requestedFlowRecord = dsl.newRecord(PROPOSED_FLOW);
        requestedFlowRecord.setFlowDef(requestBody);
        requestedFlowRecord.setCreatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        requestedFlowRecord.setCreatedBy(username);
        requestedFlowRecord.setSourceEntityId(proposedFlowCommand.source().id());
        requestedFlowRecord.setSourceEntityKind(proposedFlowCommand.source().kind().name());
        requestedFlowRecord.setTargetEntityId(proposedFlowCommand.target().id());
        requestedFlowRecord.setTargetEntityKind(proposedFlowCommand.target().kind().name());
        requestedFlowRecord.store();
        return requestedFlowRecord.getId();
    }
}
