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

package com.khartec.waltz.data.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.complexity.Complexity;
import com.khartec.waltz.model.complexity.ComplexityTotal;
import com.khartec.waltz.model.complexity.ImmutableComplexity;
import com.khartec.waltz.model.complexity.ImmutableComplexityTotal;
import com.khartec.waltz.schema.tables.records.ComplexityRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Set;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.JooqUtilities.selectorToCTE;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.COMPLEXITY;
import static com.khartec.waltz.schema.Tables.COMPLEXITY_KIND;
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


    public Tuple2<BigDecimal, BigDecimal> getAverageAndTotalScoreByKindAndSelector(Long complexityKindId, GenericSelector genericSelector) {
        Field<BigDecimal> total_complexity = DSL.sum(COMPLEXITY.SCORE).as("total_complexity");
        Field<BigDecimal> average_complexity = DSL.sum(COMPLEXITY.SCORE).divide(DSL.countDistinct(COMPLEXITY.ENTITY_ID)).as("average_complexity");
        return dsl
                .select(total_complexity)
                .select(average_complexity)
                .from(COMPLEXITY)
                .where(COMPLEXITY.COMPLEXITY_KIND_ID.eq(complexityKindId)
                        .and(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                                .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name()))))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID)
                .fetchOne(r -> tuple(r.get(average_complexity), r.get(total_complexity)));
    }


    public Set<ComplexityTotal> findTotalsByGenericSelector(GenericSelector genericSelector) {

        Field<BigDecimal> total_complexity = DSL.sum(COMPLEXITY.SCORE).as("total_complexity");
        Field<BigDecimal> average_complexity = DSL.sum(COMPLEXITY.SCORE).divide(DSL.countDistinct(COMPLEXITY.ENTITY_ID)).as("average_complexity");
        SelectHavingStep<Record3<Long, BigDecimal, BigDecimal>> totalsAndAverages = DSL
                .select( COMPLEXITY.COMPLEXITY_KIND_ID, total_complexity, average_complexity)
                .from(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())))
                .groupBy(COMPLEXITY.COMPLEXITY_KIND_ID);

        SelectOnConditionStep<Record> qry = dsl
                .select(totalsAndAverages.fields())
                .select(COMPLEXITY_KIND.fields())
                .from(totalsAndAverages)
                .innerJoin(COMPLEXITY_KIND).on(COMPLEXITY_KIND.ID.eq(totalsAndAverages.field(COMPLEXITY.COMPLEXITY_KIND_ID)));

        return qry
            .fetchSet(r -> ImmutableComplexityTotal
                    .builder()
                    .average(r.get(totalsAndAverages.field(average_complexity)))
                    .total(r.get(totalsAndAverages.field(total_complexity)))
                    .complexityKind(ComplexityKindDao.TO_COMPLEXITY_KIND_MAPPER.map(r))
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
}