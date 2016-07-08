package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entity_statistic.*;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.schema.tables.records.EntityStatisticDefinitionRecord;
import com.khartec.waltz.schema.tables.records.EntityStatisticValueRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static org.jooq.impl.DSL.count;

@Repository
public class EntityStatisticDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition es = ENTITY_STATISTIC_DEFINITION.as("es");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    private static final RecordMapper<? super Record, EntityStatisticDefinition> TO_DEFINITION_MAPPER = r -> {
        EntityStatisticDefinitionRecord record = r.into(ENTITY_STATISTIC_DEFINITION);

        return ImmutableEntityStatisticDefinition.builder()
                .id(record.getId())
                .name(record.getName())
                .description((record.getDescription()))
                .type(StatisticType.valueOf(record.getType()))
                .category(StatisticCategory.valueOf(record.getCategory()))
                .active(record.getActive())
                .renderer(record.getRenderer())
                .historicRenderer(record.getHistoricRenderer())
                .provenance(record.getProvenance())
                .build();
    };


    private static final RecordMapper<? super Record, EntityStatisticValue> TO_VALUE_MAPPPER = r -> {
        EntityStatisticValueRecord record = r.into(ENTITY_STATISTIC_VALUE);
        return  ImmutableEntityStatisticValue.builder()
                .id(record.getId())
                .statisticId(record.getStatisticId())
                .entity(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .build())
                .value(record.getValue())
                .outcome(record.getOutcome())
                .state(StatisticValueState.valueOf(record.getState()))
                .reason(record.getReason())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .current(record.getCurrent())
                .provenance(record.getProvenance())
                .build();
    };



    private static final Function<EntityStatisticDefinition, EntityStatisticDefinitionRecord> TO_RECORD_MAPPER = domainObj -> {
        EntityStatisticDefinitionRecord record = new EntityStatisticDefinitionRecord();

        record.setName(domainObj.name());
        record.setDescription(domainObj.description());
        record.setType(domainObj.type().name());
        record.setCategory(domainObj.category().name());
        record.setActive(domainObj.active());
        record.setRenderer(domainObj.renderer());
        record.setHistoricRenderer(domainObj.historicRenderer());
        record.setProvenance(domainObj.provenance());

        return record;
    };


    private static final RecordMapper<? super Record, EntityStatistic> TO_COMPOUND_MAPPER = record -> {
        return ImmutableEntityStatistic.builder()
                .definition(TO_DEFINITION_MAPPER.map(record))
                .value(TO_VALUE_MAPPPER.map(record))
                .build();
    };


    private static Field<Integer> COUNT = DSL.field("count", Integer.class);


    private static final Function<? super Map.Entry<Record, Result<Record>>, EntityStatisticSummary> TO_SUMMARY_MAPPER = recordResultEntry -> {
        EntityStatisticDefinition def = TO_DEFINITION_MAPPER.map(recordResultEntry.getKey());

        List<StringTally> counts = recordResultEntry.getValue()
                .into(esv.field(esv.OUTCOME), COUNT)
                .map(TO_STRING_TALLY);

        return ImmutableEntityStatisticSummary.builder()
                .definition(def)
                .counts(counts)
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityStatisticDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public boolean addEntityStatistic(EntityStatisticDefinition entityStatistic) {
        checkNotNull(entityStatistic, "entityStatistic cannot be null");
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(entityStatistic)) == 1;
    }


    public int[] bulkSaveValues(List<EntityStatisticValue> values) {
        return dsl
                .batch(values.stream()
                        .map(s -> dsl
                                .insertInto(
                                        ENTITY_STATISTIC_VALUE,
                                        ENTITY_STATISTIC_VALUE.STATISTIC_ID,
                                        ENTITY_STATISTIC_VALUE.ENTITY_KIND,
                                        ENTITY_STATISTIC_VALUE.ENTITY_ID,
                                        ENTITY_STATISTIC_VALUE.VALUE,
                                        ENTITY_STATISTIC_VALUE.OUTCOME,
                                        ENTITY_STATISTIC_VALUE.STATE,
                                        ENTITY_STATISTIC_VALUE.REASON,
                                        ENTITY_STATISTIC_VALUE.CREATED_AT,
                                        ENTITY_STATISTIC_VALUE.CURRENT,
                                        ENTITY_STATISTIC_VALUE.PROVENANCE)
                                .values(
                                        s.statisticId(),
                                        s.entity().kind().name(),
                                        s.entity().id(),
                                        s.value(),
                                        s.outcome(),
                                        s.state().name(),
                                        s.reason(),
                                        Timestamp.valueOf(s.createdAt()),
                                        s.current(),
                                        s.provenance()))
                        .collect(Collectors.toList()))
                .execute();
    }


    public List<EntityStatisticSummary> findForAppIdSelector(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        // aggregate query
        SelectHavingStep<Record3<Long, String, Integer>> aggregates = dsl.select(esv.STATISTIC_ID, esv.OUTCOME, count().as("count"))
                .from(esv)
                .innerJoin(es)
                .on(esv.STATISTIC_ID.eq(es.ID))
                .where(es.ACTIVE.eq(true)
                        .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(esv.ENTITY_ID.in(appIdSelector))
                        .and(esv.CURRENT.eq(true)))
                .groupBy(esv.STATISTIC_ID, esv.OUTCOME);

        // combine with definitions
        return dsl.select(es.fields())
                .select(aggregates.fields())
                .from(es)
                .innerJoin(aggregates)
                .on(es.ID.eq((Field<Long>) aggregates.field("statistic_id")))
                .fetch()
                .intoGroups(ENTITY_STATISTIC_DEFINITION.fields())
                .entrySet()
                .stream()
                .map(TO_SUMMARY_MAPPER)
                .collect(Collectors.toList());
    }


    public List<EntityStatistic> findStatisticsForEntity(EntityReference ref, boolean active) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.select(es.fields())
                .select(esv.fields())
                .from(es)
                .innerJoin(esv)
                .on(esv.STATISTIC_ID.eq(es.ID))
                .where(es.ACTIVE.eq(active)
                        .and(esv.ENTITY_KIND.eq(ref.kind().name()))
                        .and(esv.ENTITY_ID.eq(ref.id()))
                        .and(esv.CURRENT.eq(true)))
                .fetch(TO_COMPOUND_MAPPER);
    }


    public List<EntityStatisticDefinition> getAllEntityStatistics() {
        return dsl.select(es.fields())
                .from(es)
                .fetch(TO_DEFINITION_MAPPER);
    }

}