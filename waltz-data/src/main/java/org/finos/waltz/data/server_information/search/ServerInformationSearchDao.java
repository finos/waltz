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

package org.finos.waltz.data.server_information.search;

import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.UnsupportedSearcher;
import org.finos.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.server_information.ServerInformation;
import org.finos.waltz.data.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.schema.Tables.SERVER_INFORMATION;


@Repository
public class ServerInformationSearchDao implements SearchDao<ServerInformation> {

    private final DSLContext dsl;
    private final FullTextSearch<ServerInformation> searcher;


    @Autowired
    public ServerInformationSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    @Override
    public List<ServerInformation> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition externalIdCondition = JooqUtilities.mkStartsWithTermSearch(SERVER_INFORMATION.EXTERNAL_ID, terms);

        List<ServerInformation> serversViaExternalId = dsl
                .select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(externalIdCondition)
                .orderBy(SERVER_INFORMATION.HOSTNAME)
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        Condition hostnameCondition = JooqUtilities.mkBasicTermSearch(SERVER_INFORMATION.HOSTNAME, terms);

        List<ServerInformation> serversViaHostname = dsl
                .select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(hostnameCondition)
                .orderBy(SERVER_INFORMATION.HOSTNAME)
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        List<ServerInformation> serversViaFullText = searcher.searchFullText(dsl, options);

        serversViaHostname.sort(SearchUtilities.mkRelevancyComparator(ServerInformation::hostname, terms.get(0)));
        serversViaExternalId.sort(SearchUtilities.mkRelevancyComparator(a -> a.externalId().orElse(null), terms.get(0)));

        return new ArrayList<>(orderedUnion(serversViaExternalId, serversViaHostname, serversViaFullText));
    }


    private FullTextSearch<ServerInformation> determineSearcher(SQLDialect dialect) {

        if (JooqUtilities.isPostgres(dialect)) {
            return new PostgresServerInformationSearch();
        }

        if (JooqUtilities.isMariaDB(dialect)) {
            return new MariaServerInformationSearch();
        }

        if (JooqUtilities.isSQLServer(dialect)) {
            return new SqlServerServerInformationSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }

}
