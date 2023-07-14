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

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.tally.ImmutableTally;
import org.finos.waltz.model.tally.Tally;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectHavingStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

@Deprecated
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
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .groupBy(MEASURABLE_RATING.ENTITY_ID);
    }

}
