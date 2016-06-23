package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entity_statistic.*;
import com.khartec.waltz.schema.tables.records.EntityStatisticRecord;
import com.khartec.waltz.schema.tables.records.EntityStatisticValueRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityStatistic.ENTITY_STATISTIC;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticDao {

    private static final com.khartec.waltz.schema.tables.EntityStatistic es = ENTITY_STATISTIC.as("es");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    private static final RecordMapper<? super Record, EntityStatistic> TO_ENTITY_STATISTIC_MAPPPER = r -> {
        EntityStatisticRecord record = r.into(ENTITY_STATISTIC);
        return ImmutableEntityStatistic.builder()
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



    private static final Function<EntityStatistic, EntityStatisticRecord> TO_RECORD_MAPPER = domainObj -> {
        EntityStatisticRecord record = new EntityStatisticRecord();

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


    private static final RecordMapper<? super Record, EntityStatisticWithValue> TO_COMPOUND_MAPPER = record -> {
        return ImmutableEntityStatisticWithValue.builder()
                .statistic(TO_ENTITY_STATISTIC_MAPPPER.map(record))
                .value(TO_VALUE_MAPPPER.map(record))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityStatisticDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityStatisticWithValue> findStatisticsForEntity(EntityReference ref, boolean active) {
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


    public List<EntityStatistic> getAllEntityStatistics() {
        return dsl.select(es.fields())
                .from(es)
                .fetch(TO_ENTITY_STATISTIC_MAPPPER);
    }


    public boolean addEntityStatistic(EntityStatistic entityStatistic) {
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
}