package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StreamUtilities;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.roadmap.ImmutableRoadmap;
import com.khartec.waltz.model.roadmap.ImmutableRoadmapAndScenarioOverview;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.schema.tables.records.RoadmapRecord;
import org.jooq.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.data.InlineSelectFieldFactory.mkNameField;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.tables.Roadmap.ROADMAP;
import static com.khartec.waltz.schema.tables.Scenario.SCENARIO;
import static com.khartec.waltz.schema.tables.ScenarioRatingItem.SCENARIO_RATING_ITEM;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class RoadmapDao {

    private static final Field<String> ROW_TYPE_NAME = mkNameField(
            ROADMAP.ROW_TYPE_ID,
            ROADMAP.ROW_TYPE_KIND,
            asSet(EntityKind.MEASURABLE_CATEGORY)).as("rowTypeName");


    private static final Field<String> COLUMN_KIND_NAME = mkNameField(
            ROADMAP.COLUMN_TYPE_ID,
            ROADMAP.COLUMN_TYPE_KIND,
            asSet(EntityKind.MEASURABLE_CATEGORY)).as("colTypeName");


    private static final RecordMapper<Record, Roadmap> TO_DOMAIN_MAPPER = r -> {
        RoadmapRecord record = r.into(RoadmapRecord.class);
        return ImmutableRoadmap.builder()
                .id(record.getId())
                .name(record.getName())
                .ratingSchemeId(record.getRatingSchemeId())
                .columnType(readRef(r, ROADMAP.COLUMN_TYPE_KIND, ROADMAP.COLUMN_TYPE_ID, COLUMN_KIND_NAME))
                .rowType(readRef(r, ROADMAP.ROW_TYPE_KIND, ROADMAP.ROW_TYPE_ID, ROW_TYPE_NAME))
                .description(record.getDescription())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public RoadmapDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Roadmap getById(long id) {
        return baseSelect()
                .where(ROADMAP.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Collection<Roadmap> findRoadmapsBySelector(Select<Record1<Long>> selector) {
        return baseSelect()
                .where(ROADMAP.ID.in(selector))
                .orderBy(ROADMAP.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Boolean updateDescription(long id, String newValue, String userId) {
        return updateField(
                id,
                ROADMAP.DESCRIPTION,
                newValue,
                userId) == 1;
    }


    public Boolean updateName(long id, String newValue, String userId) {
        return updateField(
                id,
                ROADMAP.NAME,
                newValue,
                userId) == 1;
    }


    // -- helpers

    private SelectJoinStep<Record> baseSelect() {
        return dsl
                .select(ROW_TYPE_NAME, COLUMN_KIND_NAME)
                .select(ROADMAP.fields())
                .from(ROADMAP);
    }


    private <T> int updateField(long id, Field<T> field, T value, String userId) {
        return dsl
                .update(ROADMAP)
                .set(field, value)
                .set(ROADMAP.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(ROADMAP.LAST_UPDATED_BY, userId)
                .where(ROADMAP.ID.eq(id))
                .execute();
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {

        Stream<Field<?>> fields = StreamUtilities.concat(
                ROADMAP.fieldStream(),
                SCENARIO.fieldStream(),
                Stream.of(ROW_TYPE_NAME, COLUMN_KIND_NAME));

        List<Tuple2<Roadmap, Scenario>> roadmapScenarioTuples = dsl
                .selectDistinct(fields.collect(Collectors.toList()))
                .from(ROADMAP)
                .innerJoin(SCENARIO)
                .on(SCENARIO.ROADMAP_ID.eq(ROADMAP.ID))
                .innerJoin(SCENARIO_RATING_ITEM)
                .on(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(SCENARIO.ID))
                .where(SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND.eq(ratedEntity.kind().name()))
                .and(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID.eq(ratedEntity.id()))
                .and(SCENARIO.LIFECYCLE_STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()))
                .fetch(r -> Tuple.tuple(TO_DOMAIN_MAPPER.map(r), ScenarioDao.TO_DOMAIN_MAPPER.map(r)));

        Map<Long, List<Scenario>> scenariosByRoadmapId = roadmapScenarioTuples
                .stream()
                .map(t -> t.v2)
                .distinct()
                .collect(groupingBy(s -> s.roadmapId()));

        return roadmapScenarioTuples
                .stream()
                .map(t -> t.v1)
                .distinct()
                .map(r -> ImmutableRoadmapAndScenarioOverview
                        .builder()
                        .roadmap(r)
                        .scenarios(scenariosByRoadmapId.get(r.id().get()))
                        .build())
                .collect(toList());
    }
}
