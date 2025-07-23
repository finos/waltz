package org.finos.waltz.data.requested_flow;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.requested_flow.RequestedFlowCommand;
import org.finos.waltz.schema.tables.records.RequestedFlowRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static org.finos.waltz.schema.tables.RequestedFlow.REQUESTED_FLOW;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class RequestedFlowDao {

    private final DSLContext dsl;


    @Autowired
    public RequestedFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public Long saveRequestedFlow(String requestBody, String username, RequestedFlowCommand requestedFlowCommand){
        RequestedFlowRecord requestedFlowRecord = dsl.newRecord(REQUESTED_FLOW);
        requestedFlowRecord.setFlowDef(requestBody);
        requestedFlowRecord.setCreatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        requestedFlowRecord.setCreatedBy(username);
        requestedFlowRecord.setSourceEntity(requestedFlowCommand.source().id());
        requestedFlowRecord.setTargetEntity(requestedFlowCommand.target().id());
        requestedFlowRecord.store();
        return requestedFlowRecord.getId();
    }
}
