package com.khartec.waltz.data.performance_metric;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Quarter;
import com.khartec.waltz.model.checkpoint.Checkpoint;
import com.khartec.waltz.model.checkpoint.GoalType;
import com.khartec.waltz.model.checkpoint.ImmutableCheckpoint;
import com.khartec.waltz.model.checkpoint.ImmutableCheckpointGoal;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.performance_metric.pack.ImmutableMetricPack;
import com.khartec.waltz.model.performance_metric.pack.ImmutableMetricPackItem;
import com.khartec.waltz.model.performance_metric.pack.MetricPack;
import com.khartec.waltz.schema.tables.PerfMetricPack;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.PerfMetricPack.PERF_METRIC_PACK;
import static com.khartec.waltz.schema.tables.PerfMetricPackCheckpoint.PERF_METRIC_PACK_CHECKPOINT;
import static com.khartec.waltz.schema.tables.PerfMetricPackItem.PERF_METRIC_PACK_ITEM;
import static com.khartec.waltz.schema.tables.PerfMetricPackItemGoal.PERF_METRIC_PACK_ITEM_GOAL;

@Repository
public class PerformanceMetricPackDao {


    private final DSLContext dsl;
    private final EntityRelationshipDao entityRelationshipDao;

    private final PerfMetricPack pack = PERF_METRIC_PACK.as("pack");
    private final PerfMetricPackCheckpoint checkpoint = PERF_METRIC_PACK_CHECKPOINT.as("checkpoint");
    private final PerfMetricPackItem item = PERF_METRIC_PACK_ITEM.as("item");
    private final PerfMetricPackItemGoal goal = PERF_METRIC_PACK_ITEM_GOAL.as("goal");
    private final com.khartec.waltz.schema.tables.EntityRelationship rel = ENTITY_RELATIONSHIP.as("rel");


    @Autowired
    public PerformanceMetricPackDao(DSLContext dsl, EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.dsl = dsl;
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public MetricPack getById(long id) {

        PerfMetricPackRecord packRecord = dsl
                .select(pack.fields())
                .from(pack)
                .where(pack.ID.eq(id))
                .fetchOneInto(pack);

        Result<PerfMetricPackCheckpointRecord> checkpoints = dsl
                .select(checkpoint.fields())
                .from(checkpoint)
                .where(checkpoint.PACK_ID.eq(id))
                .orderBy(checkpoint.YEAR.asc(), checkpoint.QUARTER.asc())
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

        List<Checkpoint> checkpoints = ListUtilities.map(
                checkpointRecords,
                cp -> ImmutableCheckpoint.builder()
                        .id(cp.getId())
                        .name(cp.getName())
                        .description(mkSafe(cp.getDescription()))
                        .year(cp.getYear())
                        .quarter(Quarter.fromInt(cp.getQuarter()))
                        .build());

        Map<Long, Collection<ImmutableCheckpointGoal>> goalsByPackItem = MapUtilities.groupBy(
                x -> x.getPackItemId(),
                x -> ImmutableCheckpointGoal.builder()
                        .checkpointId(x.getCheckpointId())
                        .goalType(GoalType.valueOf(x.getKind()))
                        .value(x.getValue().doubleValue())
                        .build(),
                goalRecords);


        ImmutableMetricPack pack = ImmutableMetricPack.builder()
                .id(id)
                .checkpoints(checkpoints)
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
                .description(mkSafe(packRecord.getDescription()))
                .build();

        return pack;
    }

    public List<EntityReference> findAllPackReferences() {

        return dsl.select(pack.NAME, pack.ID)
                .from(pack)
                .stream()
                .map(p -> ImmutableEntityReference.builder()
                        .name(p.value1())
                        .kind(EntityKind.PERFORMANCE_METRIC_PACK)
                        .id(p.value2())
                        .build())
                .collect(Collectors.toList());

    }
}
