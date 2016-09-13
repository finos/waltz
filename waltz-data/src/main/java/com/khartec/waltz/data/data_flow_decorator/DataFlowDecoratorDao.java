package com.khartec.waltz.data.data_flow_decorator;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.ImmutableDecoratorRatingSummary;
import com.khartec.waltz.schema.tables.DataFlow;
import com.khartec.waltz.schema.tables.records.DataFlowDecoratorRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;
import static com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR;
import static java.util.stream.Collectors.toList;


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

    private static final Function<DataFlowDecorator, DataFlowDecoratorRecord> TO_RECORD = d -> {
        DataFlowDecoratorRecord r = new DataFlowDecoratorRecord();
        r.setDecoratorEntityKind(d.decoratorEntity().kind().name());
        r.setDecoratorEntityId(d.decoratorEntity().id());
        r.setDataFlowId(d.dataFlowId());
        r.setProvenance(d.provenance());
        r.setRating(d.rating().name());
        return r;
    };

    private final DSLContext dsl;


    @Autowired
    public DataFlowDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    // --- FINDERS ---

    public List<DataFlowDecorator> findByAppIdSelectorAndKind(Select<Record1<Long>> appIdSelector,
                                                              EntityKind decoratorEntityKind) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
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


    public List<DataFlowDecorator> findByDecoratorEntityIdSelectorAndKind(Select<Record1<Long>> decoratorEntityIdSelector,
                                                                          EntityKind decoratorKind) {
        checkNotNull(decoratorEntityIdSelector, "decoratorEntityIdSelector cannot be null");
        checkNotNull(decoratorKind, "decoratorKind cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorKind.name())
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(decoratorEntityIdSelector));

        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public List<DataFlowDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds);

        return findByCondition(condition);
    }


    public List<DataFlowDecorator> findByFlowIdsAndKind(Collection<Long> flowIds,
                                                        EntityKind decoratorEntityKind) {
        checkNotNull(flowIds, "flowIds cannot be null");
        checkNotNull(decoratorEntityKind, "decoratorEntityKind cannot be null");

        Condition condition = DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds)
                .and(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(decoratorEntityKind.name()));

        return findByCondition(condition);
    }


    public Collection<DataFlowDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = DATA_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .or(DATA_FLOW.SOURCE_ENTITY_ID.in(appIdSelector));

        return dsl.select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(DATA_FLOW)
                .on(DATA_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID))
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }


    public Map<Long, Collection<EntityReference>> findOriginatorsByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector) {
        DataFlow df = DataFlow.DATA_FLOW.as("df");
        DataFlow df2 = DataFlow.DATA_FLOW.as("df2");
        com.khartec.waltz.schema.tables.DataFlowDecorator dfd = com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR.as("dfd");
        com.khartec.waltz.schema.tables.DataFlowDecorator dfd2 = com.khartec.waltz.schema.tables.DataFlowDecorator.DATA_FLOW_DECORATOR.as("dfd2");
        com.khartec.waltz.schema.tables.Application app = com.khartec.waltz.schema.tables.Application.APPLICATION.as("app");

        Condition condition = dfd.DECORATOR_ENTITY_ID.in(dataTypeIdSelector)
                .and(dfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(df.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(df.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(df.SOURCE_ENTITY_ID.notIn(
                        DSL.select(df2.TARGET_ENTITY_ID)
                                .from(df2)
                                .innerJoin(dfd2)
                                .on(dfd2.DATA_FLOW_ID.eq(df2.ID))
                                .where(dfd2.DECORATOR_ENTITY_ID.eq(dfd.DECORATOR_ENTITY_ID))
                                .and(dfd2.DECORATOR_ENTITY_KIND.eq(dfd.DECORATOR_ENTITY_KIND))));

        Result<Record3<Long, String, Long>> origins = dsl.selectDistinct(app.ID, app.NAME, dfd.DECORATOR_ENTITY_ID)
                .from(df)
                .innerJoin(dfd).on(dfd.DATA_FLOW_ID.eq(df.ID))
                .innerJoin(app).on(app.ID.eq(df.SOURCE_ENTITY_ID))
                .where(dsl.renderInlined(condition))
                .fetch();

        Map<Long, Collection<EntityReference>> originsByDataTypeId = MapUtilities.groupBy(
                r -> r.getValue(dfd.DECORATOR_ENTITY_ID),
                r -> EntityReference.mkRef(
                        EntityKind.APPLICATION,
                        r.getValue(app.ID),
                        r.getValue(app.NAME)),
                origins);

        return originsByDataTypeId;
    }

    // --- STATS ---

    public List<DecoratorRatingSummary> summarizeForSelector(Select<Record1<Long>> selector) {
        // this is intentionally TARGET only as we use to calculate auth source stats
        Condition condition = DATA_FLOW.TARGET_ENTITY_ID.in(selector);

        Condition dataFlowJoinCondition = DATA_FLOW.ID.eq(DATA_FLOW_DECORATOR.DATA_FLOW_ID);

        Collection<Field<?>> groupingFields = newArrayList(
                DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
                DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                DATA_FLOW_DECORATOR.RATING);

        Field<Integer> countField = DSL.count(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID).as("count");

        return dsl.select(groupingFields)
                .select(countField)
                .from(DATA_FLOW_DECORATOR)
                .innerJoin(DATA_FLOW)
                .on(dsl.renderInlined(dataFlowJoinCondition))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingFields)
                .fetch(r -> {
                    EntityKind decoratorEntityKind = EntityKind.valueOf(r.getValue(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_KIND));
                    long decoratorEntityId = r.getValue(DATA_FLOW_DECORATOR.DECORATOR_ENTITY_ID);

                    EntityReference decoratorRef = EntityReference.mkRef(decoratorEntityKind, decoratorEntityId);
                    Rating rating = Rating.valueOf(r.getValue(DATA_FLOW_DECORATOR.RATING));
                    Integer count = r.getValue(countField);

                    return ImmutableDecoratorRatingSummary.builder()
                            .decoratorEntityReference(decoratorRef)
                            .rating(rating)
                            .count(count)
                            .build();
                });
    }


    // --- UPDATERS ---

    public int[] deleteDecorators(Long flowId, Collection<EntityReference> decoratorReferences) {
        List<DataFlowDecoratorRecord> records = decoratorReferences
                .stream()
                .map(ref -> {
                    DataFlowDecoratorRecord record = dsl.newRecord(DATA_FLOW_DECORATOR);
                    record.setDataFlowId(flowId);
                    record.setDecoratorEntityId(ref.id());
                    record.setDecoratorEntityKind(ref.kind().name());
                    return record;
                })
                .collect(toList());
        return dsl
                .batchDelete(records)
                .execute();
    }


    public int removeAllDecoratorsForFlowIds(List<Long> flowIds) {
        return dsl.deleteFrom(DATA_FLOW_DECORATOR)
                .where(DATA_FLOW_DECORATOR.DATA_FLOW_ID.in(flowIds))
                .execute();
    }


    public int[] addDecorators(Collection<DataFlowDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");

        List<DataFlowDecoratorRecord> records = decorators.stream()
                .map(TO_RECORD)
                .collect(toList());

        return dsl.batchInsert(records).execute();
    }


    public int[] updateDecorators(Set<DataFlowDecorator> decorators) {
        Set<DataFlowDecoratorRecord> records = SetUtilities.map(decorators, TO_RECORD);
        return dsl.batchUpdate(records).execute();
    }


    // --- HELPERS ---

    private List<DataFlowDecorator> findByCondition(Condition condition) {
        return dsl
                .select(DATA_FLOW_DECORATOR.fields())
                .from(DATA_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }

}
