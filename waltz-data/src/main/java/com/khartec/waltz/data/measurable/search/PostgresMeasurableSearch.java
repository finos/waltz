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
                        DSL.lower(MEASURABLE.DESCRIPTION),
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
