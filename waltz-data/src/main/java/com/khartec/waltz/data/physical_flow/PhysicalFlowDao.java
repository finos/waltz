package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow.FrequencyKind;
import com.khartec.waltz.model.physical_flow.ImmutablePhysicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow.TransportKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;


@Repository
public class PhysicalFlowDao {

    public static final RecordMapper<Record, PhysicalFlow> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowRecord record = r.into(PHYSICAL_FLOW);
        return ImmutablePhysicalFlow.builder()
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
    public PhysicalFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {

        checkNotNull(ref, "ref cannot be null");

        Condition matchingSource = DATA_FLOW.SOURCE_ENTITY_ID.eq(ref.id())
                .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(ref.kind().name()));
        Condition matchingTarget = DATA_FLOW.TARGET_ENTITY_ID.eq(ref.id())
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(ref.kind().name()));

        return dsl.select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .innerJoin(DATA_FLOW)
                .on(DATA_FLOW.ID.eq(PHYSICAL_FLOW.FLOW_ID))
                .where(matchingSource.or(matchingTarget))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<PhysicalFlow> findByArticleId(long articleId) {
        return findByCondition(PHYSICAL_FLOW.ARTICLE_ID.eq(articleId));
    }


    public List<PhysicalFlow> findBySelector(Select<Record1<Long>> selector) {
        return findByCondition(PHYSICAL_FLOW.ID.in(selector));
    }


    private List<PhysicalFlow> findByCondition(Condition condition) {
        return dsl.select(PHYSICAL_FLOW.fields())
                .from(PHYSICAL_FLOW)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
