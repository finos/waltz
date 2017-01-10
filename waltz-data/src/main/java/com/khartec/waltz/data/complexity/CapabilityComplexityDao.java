/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;

@Repository
public class CapabilityComplexityDao {

    private static final Field<BigDecimal> SCORE_ALIAS = DSL.field("score", BigDecimal.class);

    private static final Field<BigDecimal> SCORE_FIELD =
            DSL.sum(
                DSL.coalesce(
                    DSL.value(1).div(
                        DSL.nullif(CAPABILITY.LEVEL, 0)), 1));

    private static final RecordMapper<Record2<Long, BigDecimal>, Tally<Long>> toScoreMapper =
            r -> ImmutableTally.<Long>builder()
                    .count(r.value2().doubleValue())
                    .id(r.value1())
                    .build();


    private final DSLContext dsl;


    @Autowired
    public CapabilityComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tally<Long>> findScores() {
        return mkSelectQueryWhere(DSL.trueCondition())
                .fetch(toScoreMapper);
    }


    public List<Tally<Long>> findScoresForAppIdSelector(Select<Record1<Long>> appIds) {
        return mkSelectQueryWhere(APP_CAPABILITY.APPLICATION_ID.in(appIds))
                .fetch(toScoreMapper);
    }


    public Tally<Long> findScoresForAppId(Long appId) {
        return mkSelectQueryWhere(APP_CAPABILITY.APPLICATION_ID.eq(appId))
                .fetchOne(toScoreMapper);
    }



    public Double findBaseline() {
        return dsl.select(DSL.max(SCORE_ALIAS))
                .from(mkSelectQueryWhere(DSL.trueCondition()))
                .fetchOne()
                .value1()
                .doubleValue();
    }



    // -- HELPER ---

    private SelectHavingStep<Record2<Long, BigDecimal>> mkSelectQueryWhere(Condition condition) {
        return dsl.select(APP_CAPABILITY.APPLICATION_ID, SCORE_FIELD.as(SCORE_ALIAS))
                .from(APP_CAPABILITY)
                .innerJoin(CAPABILITY)
                .on(CAPABILITY.ID.eq(APP_CAPABILITY.CAPABILITY_ID))
                .where(condition)
                .groupBy(APP_CAPABILITY.APPLICATION_ID);
    }

}
