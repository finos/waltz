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

import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.UnsupportedSearcher;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.data.JooqUtilities.*;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;

@Repository
public class MeasurableSearchDao implements SearchDao<Measurable> {


    private final DSLContext dsl;
    private final FullTextSearch<Measurable> searcher;


    @Autowired
    public MeasurableSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    @Override
    public List<Measurable> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(lower(options.searchQuery()));

        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition externalIdCondition = mkStartsWithTermSearch(MEASURABLE.EXTERNAL_ID, terms);
        Condition nameCondition = mkBasicTermSearch(MEASURABLE.NAME, terms);

        Condition entityLifecycleCondition = MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        List<Measurable> measurablesViaExternalId = dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(externalIdCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.EXTERNAL_ID)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        List<Measurable> measurablesViaName = dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(nameCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.NAME)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        List<Measurable> measurablesViaFullText = searcher.searchFullText(dsl, options);

        return concat(
                measurablesViaExternalId,
                measurablesViaName,
                measurablesViaFullText);
    }


    private FullTextSearch<Measurable> determineSearcher(SQLDialect dialect) {

        if (isPostgres(dialect)) {
            return new PostgresMeasurableSearch();
        }

        if (isMariaDB(dialect)) {
            return new MariaMeasurableSearch();
        }

        if (isSQLServer(dialect)) {
            return new SqlServerMeasurableSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
