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
