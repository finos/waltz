/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.roadmap;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.scenario.ScenarioDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.roadmap.ImmutableRoadmap;
import org.finos.waltz.model.roadmap.ImmutableRoadmapAndScenarioOverview;
import org.finos.waltz.model.roadmap.Roadmap;
import org.finos.waltz.model.roadmap.RoadmapAndScenarioOverview;
import org.finos.waltz.model.scenario.Scenario;
import org.finos.waltz.schema.tables.records.RoadmapRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StreamUtilities.concat;
import static org.finos.waltz.data.InlineSelectFieldFactory.mkNameField;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.Roadmap.ROADMAP;
import static org.finos.waltz.schema.tables.Scenario.SCENARIO;
import static org.finos.waltz.schema.tables.ScenarioRatingItem.SCENARIO_RATING_ITEM;
import static org.jooq.lambda.tuple.Tuple.tuple;

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
                .entityLifecycleStatus(readEnum(record.getEntityLifecycleStatus(), EntityLifecycleStatus.class, s -> EntityLifecycleStatus.ACTIVE))
                .build();
    };

    public static final Condition IS_ACTIVE = ROADMAP.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name());


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
                .and(IS_ACTIVE)
                .orderBy(ROADMAP.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<Roadmap> findAll() {
        return baseSelect()
                .orderBy(ROADMAP.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }

    public Collection<Roadmap> findAllActive() {
        return baseSelect()
                .where(IS_ACTIVE)
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


    public Boolean updateLifecycleStatus(long id, EntityLifecycleStatus newValue, String userId) {
        return updateField(
                id,
                ROADMAP.ENTITY_LIFECYCLE_STATUS,
                newValue.name(),
                userId) == 1;
    }


    public Collection<RoadmapAndScenarioOverview> findAllRoadmapsAndScenarios() {
        return findRoadmapsAndScenariosViaCondition(DSL.trueCondition());
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {
        SelectConditionStep<Record1<Long>> roadmapSelector = DSL
                .select(SCENARIO.ROADMAP_ID)
                .from(SCENARIO_RATING_ITEM)
                .innerJoin(SCENARIO).on(SCENARIO.ID.eq(SCENARIO_RATING_ITEM.SCENARIO_ID))
                .where(SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND.eq(ratedEntity.kind().name()))
                .and(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID.eq(ratedEntity.id()));

        return findRoadmapsAndScenariosViaRoadmapSelector(roadmapSelector);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByFormalRelationship(EntityReference relatedEntity) {

        SelectConditionStep<Record1<Long>> roadmapIdSelector = DSL
                .select(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(relatedEntity.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(relatedEntity.id()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.ROADMAP.name()));

        return findRoadmapsAndScenariosViaRoadmapSelector(roadmapIdSelector);
    }


    public long createRoadmap(String name,
                              long ratingSchemeId,
                              EntityReference columnType,
                              EntityReference rowType,
                              String userId)
    {
        RoadmapRecord newRecord = dsl.newRecord(ROADMAP);

        newRecord.setName(name);
        newRecord.setRatingSchemeId(ratingSchemeId);
        newRecord.setColumnTypeKind(columnType.kind().name());
        newRecord.setColumnTypeId(columnType.id());
        newRecord.setRowTypeKind(rowType.kind().name());
        newRecord.setRowTypeId(rowType.id());
        newRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        newRecord.setLastUpdatedBy(userId);

        newRecord.store();

        return newRecord.getId();
    }


    // -- helpers


    private Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosViaRoadmapSelector(SelectConditionStep<Record1<Long>> roadmapIdSelector) {
        Condition condition = ROADMAP.ID.in(roadmapIdSelector);
        return findRoadmapsAndScenariosViaCondition(condition);
    }


    private List<RoadmapAndScenarioOverview> findRoadmapsAndScenariosViaCondition(Condition condition) {
        List<Field<?>> fields = concat(
                    ROADMAP.fieldStream(),
                    SCENARIO.fieldStream(),
                    Stream.of(ROW_TYPE_NAME, COLUMN_KIND_NAME))
                .collect(Collectors.toList());

        Condition scenarioActive = DSL
                .coalesce(SCENARIO.LIFECYCLE_STATUS, "") // coalesce as scenario is null when there are no scenarios
                .notEqual(EntityLifecycleStatus.REMOVED.name());

        SelectConditionStep<Record> qry = dsl
                .selectDistinct(fields)
                .from(ROADMAP)
                .leftJoin(SCENARIO)
                .on(SCENARIO.ROADMAP_ID.eq(ROADMAP.ID))
                .where(condition)
                .and(IS_ACTIVE)
                .and(scenarioActive);

        List<Tuple2<Roadmap, Scenario>> roadmapScenarioTuples = qry.fetch(
                r -> tuple(
                        RoadmapDao.TO_DOMAIN_MAPPER.map(r),  // roadmap
                        r.get(SCENARIO.ID) != null ? ScenarioDao.TO_DOMAIN_MAPPER.map(r) : null)); // scenario

        Map<Long, List<Scenario>> scenariosByRoadmapId = roadmapScenarioTuples
                .stream()
                .map(t -> t.v2)
                .distinct()
                .filter(Objects::nonNull)
                .collect(groupingBy(Scenario::roadmapId));

        return roadmapScenarioTuples
                .stream()
                .map(t -> t.v1)
                .distinct()
                .sorted(Comparator.comparing(NameProvider::name))
                .map(r -> ImmutableRoadmapAndScenarioOverview
                        .builder()
                        .roadmap(r)
                        .scenarios(r.id()
                                .map(id -> scenariosByRoadmapId.getOrDefault(id, emptyList()))
                                .orElse(emptyList()))
                        .build())
                .collect(toList());
    }


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

}
