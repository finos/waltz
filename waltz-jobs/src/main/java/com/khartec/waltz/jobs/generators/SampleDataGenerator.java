/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.jobs.generators;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

public interface SampleDataGenerator {

    String SAMPLE_DATA_PROVENANCE = "waltz-sample";
    String SAMPLE_DATA_USER = "admin";

    int NUM_APPS = 500;
    int NUM_CHANGE_INITIATIVES = 100;
    int NUM_PROCESS_GROUPS = 3;
    int NUM_PROCESSES_IN_GROUP = 6;
    int MAX_RATINGS_PER_APP = 12;


    default DSLContext getDsl(ApplicationContext ctx) {
        return ctx.getBean(DSLContext.class);
    }


    default void log(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }


    Map<String, Integer> create(ApplicationContext ctx);


    boolean remove(ApplicationContext ctx);


    default List<Long> getAppIds(DSLContext dsl) {
        return loadAllIds(dsl, APPLICATION.ID);
    }


    default <T> List<T> loadAllIds(DSLContext dsl,
                                   TableField<? extends Record, T> idCol) {
        return loadAllIds(dsl, idCol, DSL.trueCondition());
    }


    default <T> List<T> loadAllIds(DSLContext dsl,
                                   TableField<? extends Record, T> idCol,
                                   Condition condition) {
        return dsl
                .select(idCol)
                .from(idCol.getTable())
                .where(condition)
                .fetch(idCol);
    }


    default boolean deleteRatingsForCategory(DSLContext dsl,
                                             long category) {
        Condition sampleMeasurableCondition = MEASURABLE.MEASURABLE_CATEGORY_ID.eq(category)
                .and(MEASURABLE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE));

        List<Long> mIds = dsl
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(sampleMeasurableCondition)
                .fetch(MEASURABLE.ID);

        dsl.deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(mIds));

        dsl.deleteFrom(MEASURABLE)
                .where(sampleMeasurableCondition)
                .execute();

        return true;
    }

}
