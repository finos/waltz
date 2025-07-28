package org.finos.waltz.data.proposed_flow;

import org.finos.waltz.schema.tables.ProposedFlow;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW;
import static org.immutables.value.internal.$guava$.base.$Preconditions.checkNotNull;

@Repository
public class ProposedFlowParticipantDao
{
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowParticipantDao.class);

    private static final Field<String> PARTICIPANT_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            PROPOSED_FLOW.FLOW_DEF);


    public static final RecordMapper<Record, ProposedFlow> TO_DOMAIN_MAPPER = r -> {
        ProposedFlowRecord record = r.into(PROPOSED_FLOW);

        return ImmutableProposedFlow.builder()
                .flowDef(record.getFlowDef())
                .build();
    };

    private final DSLContext dsl;

    @Autowired
    public ProposedFlowParticipantDao(DSLContext dsl)
    {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public Collection<ProposedFlow> findByProposedFlowId(long id)
    {
        return findByCondition(PROPOSED_FLOW.ID.eq(id));
    }

    private Collection<ProposedFlow> findByCondition(Condition condition)
    {
        return dsl
                .select(PROPOSED_FLOW.fields())
                .select(PARTICIPANT_NAME_FIELD)
                .from(PROPOSED_FLOW)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
