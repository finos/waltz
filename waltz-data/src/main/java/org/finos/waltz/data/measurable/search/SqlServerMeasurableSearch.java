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

package org.finos.waltz.data.measurable.search;

import org.finos.waltz.data.DatabaseVendorSpecific;
import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.finos.waltz.data.SearchUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.util.List;

import static org.finos.waltz.common.StringUtilities.lower;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;

public class SqlServerMeasurableSearch implements FullTextSearch<Measurable>, DatabaseVendorSpecific {

    @Override
    public List<Measurable> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(lower(options.searchQuery()));
        Condition entityLifecycleCondition = MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        return dsl
                .selectFrom(MEASURABLE)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .and(entityLifecycleCondition)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);
    }

}
