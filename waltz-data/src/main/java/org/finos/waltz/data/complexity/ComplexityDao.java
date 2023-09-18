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

package org.finos.waltz.data.complexity;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StreamUtilities.Siphon;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.complexity.Complexity;
import org.finos.waltz.model.complexity.ComplexityTotal;
import org.finos.waltz.model.complexity.ImmutableComplexity;
import org.finos.waltz.model.complexity.ImmutableComplexityTotal;
import org.finos.waltz.model.complexity.MeasurableComplexityDetail;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.records.ComplexityRecord;
import org.jooq.AggregateFunction;
import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Record5;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.data.JooqUtilities.isPostgres;
import static org.finos.waltz.data.JooqUtilities.selectorToCTE;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.COMPLEXITY;
import static org.finos.waltz.schema.Tables.COMPLEXITY_KIND;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class ComplexityDao {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexityDao.class);

    public static final String PROVENANCE = "MeasurableComplexityRebuild";

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            COMPLEXITY.ENTITY_ID,
            COMPLEXITY.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private static final RecordMapper<Record, Complexity> TO_COMPLEXITY_MAPPER = r -> {
        ComplexityRecord record = r.into(COMPLEXITY);
        EntityReference entityReference = mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId(), r.getValue(ENTITY_NAME_FIELD));
        return ImmutableComplexity.builder()
                .id(record.getId())
                .complexityKindId(record.getComplexityKindId())
                .entityReference(entityReference)
                .score(record.getScore())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public ComplexityDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Set<Complexity> findByEntityReference(EntityReference ref){
        return dsl
                .select(COMPLEXITY.fields())
                .select(ENTITY_NAME_FIELD)
                .from(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.eq(ref.id())
                        .and(COMPLEXITY.ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_COMPLEXITY_MAPPER);
    }


    public Set<Complexity> findBySelector(GenericSelector genericSelector) {
        return dsl
                .select(ENTITY_NAME_FIELD)
                .select(COMPLEXITY.fields())
                .from(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())))
                .fetchSet(TO_COMPLEXITY_MAPPER);
    }


    public Set<Complexity> findTopComplexityScoresForKindAndSelector(long complexityKindId,
                                                                     GenericSelector genericSelector,
                                                                     int limit){
        return dsl
                .select(ENTITY_NAME_FIELD)
                .select(COMPLEXITY.fields())
                .from(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())))
                .and(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId))
                .orderBy(COMPLEXITY.SCORE.desc())
                .limit(limit)
                .fetchSet(TO_COMPLEXITY_MAPPER);
    }


    public Tuple3<BigDecimal, BigDecimal, BigDecimal> getAverageAndTotalAndMedianScoreByKindAndSelector(Long complexityKindId, GenericSelector genericSelector) {

        Field<BigDecimal> total_complexity = DSL.sum(COMPLEXITY.SCORE).as("total_complexity");
        Field<BigDecimal> average_complexity = DSL.sum(COMPLEXITY.SCORE).divide(DSL.countDistinct(COMPLEXITY.ENTITY_ID)).as("average_complexity");
        Field<BigDecimal> median_complexity = isPostgres(dsl.dialect())
                ? DSL.percentileCont(0.5).withinGroupOrderBy(COMPLEXITY.SCORE).as("median_complexity")
                : DSL.percentileCont(0.5).withinGroupOrderBy(COMPLEXITY.SCORE).over().as("median_complexity");

        SelectConditionStep<Record1<BigDecimal>> median_complexities = dsl
                .select(median_complexity)
                .from(COMPLEXITY)
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))));

        AggregateFunction<BigDecimal> grouped_median_complexity = DSL.max(median_complexities.field("median_complexity", BigDecimal.class).as("median_complexity"));

        SelectHavingStep<Record> qry = dsl
                .select(total_complexity)
                .select(average_complexity)
                .select(grouped_median_complexity)
                .from(COMPLEXITY)
                .leftJoin(median_complexities).on(DSL.trueCondition())
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID);

        return qry
                .fetchOne(r -> tuple(
                        r.get(average_complexity),
                        r.get(total_complexity),
                        r.get(grouped_median_complexity)));
    }


    public Set<ComplexityTotal> findTotalsByGenericSelector(GenericSelector genericSelector) {

        Field<BigDecimal> total_complexity = DSL.sum(COMPLEXITY.SCORE).as("total_complexity");
        Field<Integer> complexity_count = DSL.count(COMPLEXITY.ID).as("complexity_count");
        Field<BigDecimal> average_complexity = DSL.sum(COMPLEXITY.SCORE).divide(DSL.countDistinct(COMPLEXITY.ENTITY_ID)).as("average_complexity");

        SelectHavingStep<Record4<Long, BigDecimal, BigDecimal, Integer>> totalsAndAveragesQry = DSL
                .select(COMPLEXITY.COMPLEXITY_KIND_ID,
                        total_complexity,
                        average_complexity,
                        complexity_count)
                .from(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID);

        SelectConditionStep<Record3<Long, Long, BigDecimal>> squaredValuesMinusMean = DSL
                .select(COMPLEXITY.COMPLEXITY_KIND_ID,
                        COMPLEXITY.ID,
                        DSL.power(COMPLEXITY.SCORE.minus(totalsAndAveragesQry.field(average_complexity)), 2).as("value"))
                .from(COMPLEXITY)
                .innerJoin(totalsAndAveragesQry).on(COMPLEXITY.COMPLEXITY_KIND_ID.eq(totalsAndAveragesQry.field(COMPLEXITY.COMPLEXITY_KIND_ID)))
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())));

        Field<BigDecimal> standardDeviation = DSL.sqrt(
                DSL.sum(squaredValuesMinusMean.field("value", BigDecimal.class))
                        .divide(DSL.count(squaredValuesMinusMean.field(COMPLEXITY.ID)))).as("standard_deviation");

        SelectHavingStep<Record> standardDeviationByKind = DSL
                .select(squaredValuesMinusMean.field(COMPLEXITY.COMPLEXITY_KIND_ID).as("kind_id"))
                .select(standardDeviation)
                .from(squaredValuesMinusMean)
                .groupBy(squaredValuesMinusMean.field(COMPLEXITY.COMPLEXITY_KIND_ID));

        return dsl
                .select(totalsAndAveragesQry.field(total_complexity),
                        totalsAndAveragesQry.field(average_complexity))
                .select(standardDeviationByKind.field(standardDeviation))
                .select(COMPLEXITY_KIND.fields())
                .from(totalsAndAveragesQry)
                .innerJoin(COMPLEXITY_KIND).on(COMPLEXITY_KIND.ID.eq(totalsAndAveragesQry.field(COMPLEXITY.COMPLEXITY_KIND_ID)))
                .innerJoin(standardDeviationByKind).on(standardDeviationByKind.field("kind_id", Long.class).eq(totalsAndAveragesQry.field(COMPLEXITY.COMPLEXITY_KIND_ID)))
                .fetchSet(r -> ImmutableComplexityTotal
                        .builder()
                        .complexityKind(ComplexityKindDao.TO_COMPLEXITY_KIND_MAPPER.map(r))
                        .total(r.get(total_complexity))
                        .average(r.get(average_complexity))
                        .standardDeviation(r.get(standardDeviationByKind.field(standardDeviation)))
                        .build());
    }


    public Tuple2<Integer, Integer> getMappedAndMissingCountsForKindBySelector(Long complexityKindId,
                                                                              GenericSelector genericSelector) {

        CommonTableExpression<Record1<Long>> entityIds = selectorToCTE("entity_ids", genericSelector);

        CommonTableExpression<Record1<Long>> entityWithComplexity = DSL
                .name("entity_with_complexity_scores")
                .fields("id")
                .as(DSL
                        .select(COMPLEXITY.ENTITY_ID)
                        .from(COMPLEXITY)
                        .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId))
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))
                        .and(COMPLEXITY.ENTITY_ID.in(DSL.select(entityIds.field(0, Long.class)).from(entityIds))));

        Field<Integer> entityCount = DSL.count().as("entity_count");
        Field<Integer> entityWithComplexityCount = DSL.count(entityWithComplexity.field(0)).as("entity_with_complexity_count");

        return dsl
                .with(entityIds)
                .with(entityWithComplexity)
                .select(entityCount,
                        entityWithComplexityCount)
                .from(entityIds)
                .leftJoin(dsl.renderInlined(entityWithComplexity))
                .on(dsl.renderInlined(entityIds.field(0, Long.class).eq(entityWithComplexity.field(0, Long.class))))
                .fetchOne(r -> tuple(
                        r.get(entityWithComplexityCount),
                        r.get(entityCount) - r.get(entityWithComplexityCount)));
    }

    public Tuple2<BigDecimal, BigDecimal> getStandardDeviationAndVariance(Long kindId, GenericSelector genericSelector, BigDecimal mean, Integer numberOfItems) {

        SelectConditionStep<Record2<Long, BigDecimal>> squaredValuesMinusMean = dsl
                .select(COMPLEXITY.COMPLEXITY_KIND_ID, DSL.power(COMPLEXITY.SCORE.minus(DSL.val(mean)), 2).as("value"))
                .from(COMPLEXITY)
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(kindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))));


        Field<BigDecimal> variance = DSL.sum(squaredValuesMinusMean.field("value", BigDecimal.class)).divide(DSL.val(numberOfItems)).as("variance");
        Field<BigDecimal> standardDeviation = DSL.sqrt(DSL.sum(squaredValuesMinusMean.field("value", BigDecimal.class)).divide(DSL.val(numberOfItems))).as("standard_deviation");

        SelectHavingStep<Record> qry = dsl
                .select(standardDeviation)
                .select(variance)
                .from(squaredValuesMinusMean)
                .groupBy(squaredValuesMinusMean.field(COMPLEXITY.COMPLEXITY_KIND_ID));

        return qry.fetchOne(r -> tuple(r.get("standard_deviation", BigDecimal.class), r.get("variance", BigDecimal.class)));
    }


    public void processMeasurableComplexity(MeasurableComplexityDetail detail,
                                            Map<String, Long> complexityKindByExternalId) {

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        Long complexityKindId = complexityKindByExternalId.get(detail.complexityKind());

        if (complexityKindId == null) {
            LOG.error("Could not rebuild measurable complexity for unknown kind: {}", detail.complexityKind());
            return;
        }

        Measurable m = MEASURABLE;
        MeasurableCategory mc = MEASURABLE_CATEGORY;

        SelectOnConditionStep<Record1<Long>> measurablesForCategory = DSL
                .select(m.ID)
                .from(m)
                .innerJoin(mc).on(m.MEASURABLE_CATEGORY_ID.eq(mc.ID).and(mc.EXTERNAL_ID.eq(detail.measurableCategory())));

        MeasurableRating mr1 = MEASURABLE_RATING.as("mr1");
        MeasurableRating mr2 = MEASURABLE_RATING.as("mr2");

        Field<Long> m1 = DSL.when(mr1.MEASURABLE_ID.lessThan(mr2.MEASURABLE_ID), mr1.MEASURABLE_ID).otherwise(mr2.MEASURABLE_ID).as("m1");
        Field<Long> m2 = DSL.when(mr1.MEASURABLE_ID.greaterThan(mr2.MEASURABLE_ID), mr1.MEASURABLE_ID).otherwise(mr2.MEASURABLE_ID).as("m2");

        SelectConditionStep<Record4<Long, String, Long, Long>> measurablePairs = DSL
                .selectDistinct(mr1.ENTITY_ID, mr1.ENTITY_KIND, m1, m2)
                .from(mr1)
                .innerJoin(mr2).on(mr1.ENTITY_ID.eq(mr2.ENTITY_ID).and(mr1.ENTITY_KIND.eq(mr2.ENTITY_KIND)))
                .where(mr1.MEASURABLE_ID.in(measurablesForCategory)
                        .and(mr2.MEASURABLE_ID.in(measurablesForCategory)));

        EntityHierarchy eh1 = ENTITY_HIERARCHY.as("eh1");
        EntityHierarchy eh2 = ENTITY_HIERARCHY.as("eh2");

        Field<Integer> joinLevel = DSL.coalesce(DSL.max(eh2.LEVEL), 0).as("joinLevel");

        SelectHavingStep<Record5<Long, String, Long, Long, Integer>> commonAncestors = DSL
                .select(measurablePairs.field(mr1.ENTITY_ID),
                        measurablePairs.field(mr1.ENTITY_KIND),
                        measurablePairs.field(m1),
                        measurablePairs.field(m2),
                        joinLevel)
                .from(measurablePairs)
                .leftJoin(eh1).on(eh1.ID.eq(measurablePairs.field(m1)).and(eh1.KIND.eq(EntityKind.MEASURABLE.name())))
                .leftJoin(eh2).on(eh2.ID.eq(measurablePairs.field(m2)).and(eh2.KIND.eq(EntityKind.MEASURABLE.name())
                        .and(eh1.ANCESTOR_ID.eq(eh2.ANCESTOR_ID))))
                .groupBy(measurablePairs.field(mr1.ENTITY_ID),
                        measurablePairs.field(mr1.ENTITY_KIND),
                        measurablePairs.field(m1),
                        measurablePairs.field(m2));

        Field<Double> rawComplexityField = DSL
                .val(1).cast(SQLDataType.DOUBLE)
                .div(DSL
                        .sum(commonAncestors.field(joinLevel)).cast(SQLDataType.DOUBLE)
                        .div(DSL.count()))
                .as("rawComplexityField");

        SelectHavingStep<Record3<Long, String, Double>> rawComplexity = dsl
                .select(commonAncestors.field(measurablePairs.field(mr1.ENTITY_ID)),
                        commonAncestors.field(measurablePairs.field(mr1.ENTITY_KIND)),
                        rawComplexityField)
                .from(commonAncestors)
                .groupBy(commonAncestors.field(measurablePairs.field(mr1.ENTITY_ID)),
                        commonAncestors.field(measurablePairs.field(mr1.ENTITY_KIND)));

        Field<Double> maxField = DSL.max(rawComplexity.field(rawComplexityField)).as("maxField");

        SelectJoinStep<Record1<Double>> maxRawComplexity = dsl
                .select(maxField)
                .from(rawComplexity);

        Field<Double> complexity = rawComplexity.field(rawComplexityField).div(maxRawComplexity.field(maxField));

        Field<Long> entityId = rawComplexity.field(commonAncestors.field(measurablePairs.field(mr1.ENTITY_ID)));
        Field<String> entityKind = rawComplexity.field(commonAncestors.field(measurablePairs.field(mr1.ENTITY_KIND)));

        SelectJoinStep<Record3<Long, String, Double>> qry = dsl
                .select(
                        entityId,
                        entityKind,
                        complexity)
                .from(rawComplexity)
                .crossJoin(maxRawComplexity);

        // entity id, entity kind and complexity
        Result<Record3<Long, String, Double>> complexitiesForCategory = qry.fetch();

        dsl.transaction(ctx -> {

            DSLContext tx = ctx.dsl();

            LOG.debug("Removing existing measurable complexities for {}", detail.complexityKind());
            int removedComplexities = tx
                    .deleteFrom(COMPLEXITY)
                    .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                            .and(COMPLEXITY.PROVENANCE.eq(PROVENANCE)))
                    .execute();

            Siphon<ComplexityRecord> unknownValueSiphon = mkSiphon(r -> r.getEntityId() == null || r.getEntityKind() == null || r.getScore() == null);

            LOG.debug("Inserting measurable complexities for {}", detail.complexityKind());
            int insertedComplexities = summarizeResults(complexitiesForCategory
                    .stream()
                    .map(d -> {

                        Double complexityScore = d.get(complexity);
                        String kind = d.get(entityKind);
                        Long id = d.get(entityId);

                        ComplexityRecord r = tx.newRecord(COMPLEXITY);

                        r.setEntityId(id);
                        r.setEntityKind(kind);
                        r.setComplexityKindId(complexityKindId);
                        r.setScore(BigDecimal.valueOf(complexityScore));
                        r.setLastUpdatedAt(now);
                        r.setLastUpdatedBy("admin");
                        r.setProvenance(PROVENANCE);

                        return r;
                    })
                    .filter(unknownValueSiphon)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), tx::batchInsert))
                    .execute());

            LOG.info(format(
                    "Updated measurable complexities for category: %s, and complexity kind: %s. %d records removed. %d records inserted. %d dodgy records.",
                    detail.measurableCategory(),
                    detail.complexityKind(),
                    removedComplexities,
                    insertedComplexities,
                    unknownValueSiphon.getResults().size()));
        });

    }
}