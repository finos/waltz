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

package com.khartec.waltz.data.measurable.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;

public class PostgresMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    @Override
    public List<Measurable> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        Condition entityLifecycleCondition = MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        Field<Double> rank = DSL
                .field("ts_rank_cd(to_tsvector({0}), plainto_tsquery({1}))",
                        Double.class,
                        MEASURABLE.DESCRIPTION.lower(),
                        DSL.inline(options.searchQuery().toLowerCase()));

        return dsl
                .select(MEASURABLE.fields())
                .select(rank)
                .from(MEASURABLE)
                .where(rank.greaterThan(Double.MIN_VALUE))
                .and(entityLifecycleCondition)
                .orderBy(rank.desc())
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);
    }

}
