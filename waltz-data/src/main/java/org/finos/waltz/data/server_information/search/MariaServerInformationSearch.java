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

import org.finos.waltz.data.DatabaseVendorSpecific;
import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.server_information.ServerInformation;
import org.finos.waltz.data.SearchUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static com.khartec.waltz.schema.Tables.SERVER_INFORMATION;

public class MariaServerInformationSearch implements FullTextSearch<ServerInformation>, DatabaseVendorSpecific {

    @Override
    public List<ServerInformation> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition externalIdCondition = terms.stream()
                .map(term -> SERVER_INFORMATION.EXTERNAL_ID.like(term + "%"))
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
                .map(term -> SERVER_INFORMATION.HOSTNAME.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        List<ServerInformation> serversViaHostname = dsl.selectDistinct(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(hostnameCondition)
                .orderBy(SERVER_INFORMATION.HOSTNAME)
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        List<ServerInformation> serversViaFullText = dsl.select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where("MATCH(hostname, external_id, operating_system, location) AGAINST (?)", options.searchQuery())
                .limit(options.limit())
                .fetch(ServerInformationDao.TO_DOMAIN_MAPPER);

        serversViaHostname.sort(SearchUtilities.mkRelevancyComparator(a -> a.hostname(), terms.get(0)));
        serversViaExternalId.sort(SearchUtilities.mkRelevancyComparator(a -> a.externalId().orElse(null), terms.get(0)));

        return new ArrayList<>(orderedUnion(serversViaExternalId, serversViaHostname, serversViaFullText));
    }
}
