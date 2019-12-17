/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.data.server_information.search;

import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.UnsupportedSearcher;
import com.khartec.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.server_information.ServerInformation;
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
import static com.khartec.waltz.schema.Tables.SERVER_INFORMATION;


@Repository
public class ServerInformationSearchDao {

    private final DSLContext dsl;
    private final FullTextSearch<ServerInformation> searcher;


    @Autowired
    public ServerInformationSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    public List<ServerInformation> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");
        List<String> terms = mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition externalIdCondition = terms.stream()
                .map(SERVER_INFORMATION.EXTERNAL_ID::startsWithIgnoreCase)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<ServerInformation> serversViaExternalId = dsl.selectDistinct(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(externalIdCondition)
                .orderBy(SERVER_INFORMATION.HOSTNAME)
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        Condition hostnameCondition = terms.stream()
                .map(SERVER_INFORMATION.HOSTNAME::containsIgnoreCase)
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<ServerInformation> serversViaHostname = dsl.selectDistinct(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(hostnameCondition)
                .orderBy(SERVER_INFORMATION.HOSTNAME)
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        List<ServerInformation> serversViaFullText = searcher.searchFullText(dsl, options);

        serversViaHostname.sort(mkRelevancyComparator(a -> a.hostname(), terms.get(0)));
        serversViaExternalId.sort(mkRelevancyComparator(a -> a.externalId().orElse(null), terms.get(0)));

        return new ArrayList<>(orderedUnion(serversViaExternalId, serversViaHostname, serversViaFullText));
    }


    private FullTextSearch<ServerInformation> determineSearcher(SQLDialect dialect) {

        if (isPostgres(dialect)) {
            return new PostgresServerInformationSearch();
        }

        if (isMariaDB(dialect)) {
            return new MariaServerInformationSearch();
        }

        if (isSQLServer(dialect)) {
            return new SqlServerServerInformationSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }

}
