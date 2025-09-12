package org.finos.waltz.data.proposed_flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;

@Repository
public class ProposedFlowDao {
    private final DSLContext dsl;
    private static final Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            PROPOSED_FLOW.SOURCE_ENTITY_ID,
            PROPOSED_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));


    private static final Field<String> TARGET_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            PROPOSED_FLOW.TARGET_ENTITY_ID,
            PROPOSED_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Field<String> SOURCE_EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            PROPOSED_FLOW.SOURCE_ENTITY_ID,
            PROPOSED_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Field<String> TARGET_EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            PROPOSED_FLOW.TARGET_ENTITY_ID,
            PROPOSED_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));


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

    public List<ProposedFlowRecord> getProposedFlows() {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .from(PROPOSED_FLOW)
                .fetchInto(ProposedFlowRecord.class);
    }
    private SelectJoinStep<Record> baseQuery() {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .select(SOURCE_EXTERNAL_ID_FIELD, TARGET_EXTERNAL_ID_FIELD)
                .from(PROPOSED_FLOW);
    }

    public List<ProposedFlowRecord> getProposedFlowsBySelector(Select<Record1<Long>> flowIdSelector) {
        return baseQuery()
                .where(dsl.renderInlined(PROPOSED_FLOW.ID.in(flowIdSelector)))
                .fetchInto(ProposedFlowRecord.class);
    }
}


