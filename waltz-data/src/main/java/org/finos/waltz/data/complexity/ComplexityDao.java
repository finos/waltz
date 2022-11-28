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
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.complexity.Complexity;
import org.finos.waltz.model.complexity.ComplexityTotal;
import org.finos.waltz.model.complexity.ImmutableComplexity;
import org.finos.waltz.model.complexity.ImmutableComplexityTotal;
import org.finos.waltz.schema.tables.records.ComplexityRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.JooqUtilities.selectorToCTE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.COMPLEXITY;
import static org.finos.waltz.schema.Tables.COMPLEXITY_KIND;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class ComplexityDao {

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
        Field<BigDecimal> median_complexity = DSL.percentileCont(0.5).withinGroupOrderBy(COMPLEXITY.SCORE).as("median_complexity");

        SelectHavingStep<Record> median_complexities = dsl
                .select(COMPLEXITY.COMPLEXITY_KIND_ID)
                .select(median_complexity)
                .from(COMPLEXITY)
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID);

        AggregateFunction<BigDecimal> grouped_median_complexity = DSL.max(median_complexities.field("median_complexity", BigDecimal.class).as("median_complexity"));

        SelectHavingStep<Record> qry = dsl
                .select(total_complexity)
                .select(average_complexity)
                .select(grouped_median_complexity)
                .from(COMPLEXITY)
                .leftJoin(median_complexities).on(COMPLEXITY.COMPLEXITY_KIND_ID.eq(median_complexities.field(COMPLEXITY.COMPLEXITY_KIND_ID)))
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID);

        return qry.fetchOne(
                r -> tuple(
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
}