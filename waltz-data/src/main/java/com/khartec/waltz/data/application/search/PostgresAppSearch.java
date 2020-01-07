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

package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;

public class PostgresAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        Field<Double> rank = DSL
                .field("ts_rank_cd(to_tsvector({0} || ' ' || coalesce({1}, '')), plainto_tsquery({2}))",
                        Double.class,
                        APPLICATION.DESCRIPTION.lower(),
                        APPLICATION.PARENT_ASSET_CODE.lower(),
                        DSL.inline(options.searchQuery().toLowerCase()));

        Condition lifecycleCondition = APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        return dsl
                .select(APPLICATION.fields())
                .select(rank)
                .from(APPLICATION)
                .where(rank.greaterThan(Double.MIN_VALUE))
                .and(lifecycleCondition)
                .orderBy(rank.desc())
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }

}
