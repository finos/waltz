/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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