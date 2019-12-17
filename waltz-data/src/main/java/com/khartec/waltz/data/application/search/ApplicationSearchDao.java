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

package com.khartec.waltz.data.application.search;

import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.data.JooqUtilities.*;
import static com.khartec.waltz.data.SearchUtilities.mkRelevancyComparator;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;

@Repository
public class ApplicationSearchDao {


    private final DSLContext dsl;
    private final FullTextSearch<Application> searcher;


    @Autowired
    public ApplicationSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<Application> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");

        List<String> terms = mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition lifecycleCondition = APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        Condition assetCodeCondition = terms.stream()
                .map(APPLICATION.ASSET_CODE::startsWith)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaAssetCode = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .where(assetCodeCondition)
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        Condition aliasCondition = terms.stream()
                .map(ENTITY_ALIAS.ALIAS::containsIgnoreCase)
                .collect(Collectors.reducing(
                        ENTITY_ALIAS.KIND.eq(EntityKind.APPLICATION.name()),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaAlias = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(APPLICATION.ID))
                .where(aliasCondition)
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);

        Condition nameCondition = terms.stream()
                .map(APPLICATION.NAME::containsIgnoreCase)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<Application> appsViaName = dsl.selectDistinct(APPLICATION.fields())
                .from(APPLICATION)
                .where(nameCondition)
                .and(lifecycleCondition)
                .orderBy(APPLICATION.NAME)
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);


        List<Application> appsViaFullText = searcher.searchFullText(dsl, options);

        appsViaName.sort(mkRelevancyComparator(NameProvider::name, terms.get(0)));

        return new ArrayList<>(orderedUnion(appsViaAssetCode, appsViaName, appsViaAlias, appsViaFullText));
    }


    private FullTextSearch<Application> determineSearcher(SQLDialect dialect) {

        if (isPostgres(dialect)) {
            return new PostgresAppSearch();
        }

        if (isMariaDB(dialect)) {
            return new MariaAppSearch();
        }

        if (isSQLServer(dialect)) {
            return new SqlServerAppSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
