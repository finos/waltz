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
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.complexity.*;
import com.khartec.waltz.schema.tables.records.ComplexityScoreRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.ComplexityScore.COMPLEXITY_SCORE;
import static java.util.Collections.emptyList;

@Repository
public class ComplexityScoreDao {

    private static final Function<Record, ComplexityScore> TO_COMPLEXITY_SCORE_MAPPER = r -> {
        ComplexityScoreRecord record = r.into(COMPLEXITY_SCORE);
        return ImmutableComplexityScore.builder()
                .kind(ComplexityKind.valueOf(record.getComplexityKind()))
                .id(record.getEntityId())
                .score(record.getScore().doubleValue())
                .build();
    };


    private static final BiFunction<Long, Collection<ComplexityScore>, ComplexityRating> TO_COMPLEXITY_RATING =
        (id, scores) -> {

            ImmutableComplexityRating.Builder builder = ImmutableComplexityRating.builder()
                    .id(id);

            scores.forEach(score -> {
                switch (score.kind()) {
                    case MEASURABLE:
                        builder.measurableComplexity(score);
                        break;
                    case CONNECTION:
                        builder.connectionComplexity(score);
                        break;
                    case SERVER:
                        builder.serverComplexity(score);
                        break;
                }
            });

            return builder.build();
    };


    private final DSLContext dsl;


    @Autowired
    public ComplexityScoreDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public ComplexityRating getForApp(long appId) {
        Map<Long, List<ComplexityScore>> scoresForApp = dsl.select(COMPLEXITY_SCORE.fields())
                .from(COMPLEXITY_SCORE)
                .where(COMPLEXITY_SCORE.ENTITY_ID.eq(appId))
                .and(COMPLEXITY_SCORE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .fetch()
                .stream()
                .map(TO_COMPLEXITY_SCORE_MAPPER)
                .collect(Collectors.groupingBy(ComplexityScore::id));

        return TO_COMPLEXITY_RATING.apply(appId, scoresForApp.getOrDefault(appId, emptyList()));
    }


    public List<ComplexityRating> findForAppIdSelector(Select<Record1<Long>> appIdSelector) {

        Condition condition = COMPLEXITY_SCORE.ENTITY_ID.in(appIdSelector)
                .and(COMPLEXITY_SCORE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Map<Long, List<ComplexityScore>> scoresForApp = dsl
                .select(COMPLEXITY_SCORE.fields())
                .from(COMPLEXITY_SCORE)
                .where(dsl.renderInlined(condition)).fetch()
                .stream()
                .map(TO_COMPLEXITY_SCORE_MAPPER)
                .collect(Collectors.groupingBy(ComplexityScore::id));

        return scoresForApp
                .entrySet()
                .stream()
                .map(entry -> TO_COMPLEXITY_RATING.apply(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    public int deleteAll() {
        return dsl.deleteFrom(COMPLEXITY_SCORE).execute();
    }

    public int[] bulkInsert(List<ComplexityScoreRecord> records) {
        return dsl.batchInsert(records).execute();
    }
}