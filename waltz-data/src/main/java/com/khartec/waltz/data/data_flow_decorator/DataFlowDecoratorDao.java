package com.khartec.waltz.data.data_flow_decorator;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.schema.tables.records.DataFlowDecoratorRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR;


@Repository
public class DataFlowDecoratorDao {

    private static final RecordMapper<Record, DataFlowDecorator> TO_DECORATOR_MAPPER = r -> {
        DataFlowDecoratorRecord record = r.into(DATA_FLOW_DECORATOR);

        return ImmutableDataFlowDecorator.builder()
                .dataFlowId(record.getDataFlowId())
                .decoratorEntity(ImmutableEntityReference.builder()
                        .id(record.getDecoratorEntityId())
                        .kind(EntityKind.valueOf(record.getDecoratorEntityKind()))
                        .build())
                .rating(Rating.valueOf(record.getRating()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public DataFlowDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DataFlowDecorator> findBySelectorAndKind(Select<Record1<Long>> appIdSelector,
                                                         EntityKind decoratorEntityKind) {

        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dsl.select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(DATA_FLOW)
                .on(DATA_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID))
                .where(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector)
                                .or(DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector)))
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()))
                .and(DATA_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(DATA_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<DataFlowDecorator> findByFlowIdsAndKind(Collection<Long> flowIds,
                                                        EntityKind decoratorEntityKind) {
        checkNotNull(flowIds, "flowIds cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dsl.select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .where(DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds))
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()))
                .fetch(TO_DECORATOR_MAPPER);
    }

}
