package org.finos.waltz.data.proposed_flow;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlow;
import org.finos.waltz.model.proposed_flow.ProposedFlow;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;

@Repository
public class ProposedFlowDao {

    public static final RecordMapper<Record, ProposedFlow> TO_DOMAIN_MAPPER = r -> {
        ProposedFlowRecord record = r.into(PROPOSED_FLOW);
        return ImmutableProposedFlow.builder()
                .id(record.getId())
                .sourceEntityId(record.getSourceEntityId())
                .sourceEntityKind(record.getSourceEntityKind())
                .targetEntityId(record.getTargetEntityId())
                .targetEntityKind(record.getTargetEntityKind())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .createdBy(record.getCreatedBy())
                .flowDef(record.getFlowDef())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public ProposedFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public Long saveProposedFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand){
        ProposedFlowRecord proposedFlowRecord = dsl.newRecord(PROPOSED_FLOW);
        proposedFlowRecord.setFlowDef(requestBody);
        proposedFlowRecord.setCreatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        proposedFlowRecord.setCreatedBy(username);
        proposedFlowRecord.setSourceEntityId(proposedFlowCommand.source().id());
        proposedFlowRecord.setSourceEntityKind(proposedFlowCommand.source().kind().name());
        proposedFlowRecord.setTargetEntityId(proposedFlowCommand.target().id());
        proposedFlowRecord.setTargetEntityKind(proposedFlowCommand.target().kind().name());
        proposedFlowRecord.store();
        return proposedFlowRecord.getId();
    }

    /**
     * Fetches the single ProposedFlow row whose primary-key equals {@code id}.
     *
     * @param id primary key of the row (e.g. 1)
     * @return Optional containing the row, or empty if not found
     */

    public ProposedFlow getProposedFlowById(long id) {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .from(PROPOSED_FLOW)
                .where(PROPOSED_FLOW.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }
}
