package com.khartec.waltz.data.physical_data_flow;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_data_flow.FrequencyKind;
import com.khartec.waltz.model.physical_data_flow.ImmutablePhysicalDataFlow;
import com.khartec.waltz.model.physical_data_flow.PhysicalDataFlow;
import com.khartec.waltz.model.physical_data_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalDataFlowRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalDataFlow.PHYSICAL_DATA_FLOW;

/**
 * Created by dwatkins on 03/10/2016.
 */
@Repository
public class PhysicalDataFlowDao {

    public static final RecordMapper<Record, PhysicalDataFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalDataFlowRecord record = r.into(PHYSICAL_DATA_FLOW);
        return ImmutablePhysicalDataFlow.builder()
                .id(record.getId())
                .provenance(record.getProvenance())
                .articleId(record.getArticleId())
                .basisOffset(record.getBasisOffset())
                .frequency(FrequencyKind.valueOf(record.getFrequency()))
                .description(record.getDescription())
                .flowId(record.getFlowId())
                .transport(TransportKind.valueOf(record.getTransport()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PhysicalDataFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public List<PhysicalDataFlow> findFlowsForEntityReference(EntityReference ref) {

        checkNotNull(ref, "ref cannot be null");

        Condition matchingSource = DATA_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()));
        Condition matchingTarget = DATA_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()));

        return dsl.select(PHYSICAL_DATA_FLOW.fields())
                .from(PHYSICAL_DATA_FLOW)
                .innerJoin(DATA_FLOW)
                .on(DATA_FLOW.ID.eq(PHYSICAL_DATA_FLOW.FLOW_ID))
                .where(matchingSource.or(matchingTarget))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
