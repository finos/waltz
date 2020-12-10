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
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

public class SqlServerAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());

        return dsl
                .selectFrom(APPLICATION)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses()))
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }
}
