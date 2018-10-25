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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

@Repository
public class MeasurableComplexityDao {

    private static final Field<BigDecimal> SCORE_ALIAS = DSL.field("score", BigDecimal.class);

    private static final Field<BigDecimal> SCORE_FIELD =
            DSL.sum(
                DSL.coalesce(
                    DSL.value(1).div(
                        DSL.nullif(ENTITY_HIERARCHY.LEVEL, 0)), 1));

    private static final RecordMapper<Record2<Long, BigDecimal>, Tally<Long>> toScoreMapper =
            r -> ImmutableTally.<Long>builder()
                    .count(r.value2().doubleValue())
                    .id(r.value1())
                    .build();


    private final DSLContext dsl;


    @Autowired
    public MeasurableComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tally<Long>> findScoresForAppIdSelector(Select<Record1<Long>> appIds) {
        Condition condition = MEASURABLE_RATING.ENTITY_ID.in(appIds)
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        return mkSelectQueryWhere(condition)
                .fetch(toScoreMapper);
    }


    public Double calculateBaseline() {
        return dsl.select(DSL.max(SCORE_ALIAS))
                .from(mkSelectQueryWhere(DSL.trueCondition()))
                .fetchOne()
                .value1()
                .doubleValue();
    }


    // -- HELPER ---

    private SelectHavingStep<Record2<Long, BigDecimal>> mkSelectQueryWhere(Condition condition) {
        return dsl.select(MEASURABLE_RATING.ENTITY_ID, SCORE_FIELD.as(SCORE_ALIAS))
                .from(MEASURABLE_RATING)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .where(condition)
                .groupBy(MEASURABLE_RATING.ENTITY_ID);
    }

}
