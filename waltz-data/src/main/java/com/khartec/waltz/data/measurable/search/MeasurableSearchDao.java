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
