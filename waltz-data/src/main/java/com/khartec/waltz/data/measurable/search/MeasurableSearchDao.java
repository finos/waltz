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

import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.common.StringUtilities.lower;
import static com.khartec.waltz.data.JooqUtilities.*;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static java.util.Collections.emptyList;

@Repository
public class MeasurableSearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<Measurable> searcher;


    @Autowired
    public MeasurableSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Measurable> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(lower(options.searchQuery()));

        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition externalIdCondition = terms.stream()
                .map(MEASURABLE.EXTERNAL_ID::containsIgnoreCase)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        Condition entityLifecycleCondition = MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        List<Measurable> measurablesViaExternalId = dsl.selectDistinct(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(externalIdCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.EXTERNAL_ID)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        Condition nameCondition = terms.stream()
                .map(MEASURABLE.NAME::containsIgnoreCase)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Measurable> measurablesViaName = dsl.selectDistinct(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(nameCondition)
                .and(entityLifecycleCondition)
                .orderBy(MEASURABLE.NAME)
                .limit(options.limit())
                .fetch(MeasurableDao.TO_DOMAIN_MAPPER);

        List<Measurable> measurablesViaFullText = searcher.searchFullText(dsl, options);

        return new ArrayList<>(orderedUnion(measurablesViaExternalId, measurablesViaName, measurablesViaFullText));
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
