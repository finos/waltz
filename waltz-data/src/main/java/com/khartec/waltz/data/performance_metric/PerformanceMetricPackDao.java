package com.khartec.waltz.data.performance_metric;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Quarter;
import com.khartec.waltz.model.checkpoint.GoalType;
import com.khartec.waltz.model.checkpoint.ImmutableCheckpoint;
import com.khartec.waltz.model.checkpoint.ImmutableCheckpointGoal;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.performance_metric.pack.ImmutableMetricPack;
import com.khartec.waltz.model.performance_metric.pack.ImmutableMetricPackItem;
import com.khartec.waltz.model.performance_metric.pack.MetricPack;
import com.khartec.waltz.schema.tables.PerfMetricPackCheckpoint;
import com.khartec.waltz.schema.tables.PerfMetricPackItem;
import com.khartec.waltz.schema.tables.PerfMetricPackItemGoal;
import com.khartec.waltz.schema.tables.records.PerfMetricPackCheckpointRecord;
import com.khartec.waltz.schema.tables.records.PerfMetricPackItemGoalRecord;
import com.khartec.waltz.schema.tables.records.PerfMetricPackItemRecord;
import com.khartec.waltz.schema.tables.records.PerfMetricPackRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PerfMetricPack.PERF_METRIC_PACK;
import static com.khartec.waltz.schema.tables.PerfMetricPackCheckpoint.PERF_METRIC_PACK_CHECKPOINT;
import static com.khartec.waltz.schema.tables.PerfMetricPackItem.PERF_METRIC_PACK_ITEM;
import static com.khartec.waltz.schema.tables.PerfMetricPackItemGoal.PERF_METRIC_PACK_ITEM_GOAL;

@Repository
public class PerformanceMetricPackDao {


    private final DSLContext dsl;
    private final EntityRelationshipDao entityRelationshipDao;


    @Autowired
    public PerformanceMetricPackDao(DSLContext dsl, EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.dsl = dsl;
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public MetricPack getById(long id) {

        PerfMetricPackCheckpoint checkpoint = PERF_METRIC_PACK_CHECKPOINT.as("checkpoint");
        PerfMetricPackItem item = PERF_METRIC_PACK_ITEM.as("item");
        PerfMetricPackItemGoal goal = PERF_METRIC_PACK_ITEM_GOAL.as("goal");


        PerfMetricPackRecord packRecord = dsl
                .select(PERF_METRIC_PACK.fields())
                .from(PERF_METRIC_PACK)
                .where(PERF_METRIC_PACK.ID.eq(id))
                .fetchOneInto(PERF_METRIC_PACK);

        Result<PerfMetricPackCheckpointRecord> checkpoints = dsl
                .select(checkpoint.fields())
                .from(checkpoint)
                .where(checkpoint.PACK_ID.eq(id))
                .fetchInto(checkpoint);

        Collection<EntityRelationship> relationships = entityRelationshipDao.findRelationshipsFrom(ImmutableEntityReference.builder()
                .kind(EntityKind.PERFORMANCE_METRIC_PACK)
                .id(id)
                .build(), RelationshipKind.RELATES_TO);

        Result<PerfMetricPackItemRecord> items = dsl
                .select(item.fields())
                .from(item)
                .where(item.PACK_ID.eq(id))
                .fetchInto(item);

        Result<PerfMetricPackItemGoalRecord> goals = dsl
                .select(goal.fields())
                .from(goal)
                .innerJoin(item).on(item.ID.eq(goal.PACK_ITEM_ID))
                .where(item.PACK_ID.eq(id))
                .fetchInto(goal);

        return buildMetricPack(id, packRecord, checkpoints, relationships, items, goals);

    }

    private MetricPack buildMetricPack(long id,
                                       PerfMetricPackRecord packRecord,
                                       Result<PerfMetricPackCheckpointRecord> checkpointRecords,
                                       Collection<EntityRelationship> relationships,
                                       Result<PerfMetricPackItemRecord> itemRecords,
                                       Result<PerfMetricPackItemGoalRecord> goalRecords) {

        Map<Long, ImmutableCheckpoint> checkpointsById = MapUtilities.indexBy(
                x -> x.getId(),
                cp -> ImmutableCheckpoint.builder()
                        .name(cp.getName())
                        .description(cp.getDescription())
                        .year(cp.getYear())
                        .quarter(Quarter.fromInt(cp.getQuarter()))
                        .build(),
                checkpointRecords);

        Map<Long, Collection<ImmutableCheckpointGoal>> goalsByPackItem = MapUtilities.groupBy(
                x -> x.getPackItemId(),
                x -> ImmutableCheckpointGoal.builder()
                        .checkpoint(checkpointsById.get(x.getCheckpointId()))
                        .goalType(GoalType.valueOf(x.getKind()))
                        .value(x.getValue().doubleValue())
                        .build(),
                goalRecords);


        ImmutableMetricPack pack = ImmutableMetricPack.builder()
                .id(id)
                .checkpoints(checkpointsById.values())
                .relatedReferences(relationships
                        .stream()
                        .map(er -> er.b())
                        .collect(Collectors.toList()))
                .items(itemRecords
                        .stream()
                        .map(i -> ImmutableMetricPackItem.builder()
                                .id(i.getId())
                                .sectionName(i.getSectionName())
                                .baseLine(i.getBaseline().doubleValue())
                                .definitionId(i.getDefinitionId())
                                .goals(goalsByPackItem.getOrDefault(i.getId(), Collections.emptyList()))
                                .build())
                        .collect(Collectors.toList()))
                .name(packRecord.getName())
                .description(packRecord.getDescription())
                .build();

        return pack;
    }
}
