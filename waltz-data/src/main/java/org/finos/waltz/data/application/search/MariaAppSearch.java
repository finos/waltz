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

package org.finos.waltz.data.application.search;

import org.finos.waltz.data.DatabaseVendorSpecific;
import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;
import static org.finos.waltz.common.ListUtilities.map;

public class MariaAppSearch implements FullTextSearch<Application>, DatabaseVendorSpecific {

    @Override
    public List<Application> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> tokens = map(SearchUtilities.mkTerms(options.searchQuery()), t -> t.toLowerCase());
        Condition lifecycleCondition = APPLICATION.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());

        return dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .where("MATCH(name, description, asset_code, parent_asset_code) AGAINST (?)", options.searchQuery())
                .and(lifecycleCondition)
                .union(DSL.selectDistinct(APPLICATION.fields())
                                .from(APPLICATION)
                                .innerJoin(ENTITY_ALIAS)
                                .on(ENTITY_ALIAS.ID.eq(APPLICATION.ID)
                                        .and(ENTITY_ALIAS.KIND.eq(EntityKind.APPLICATION.name())))
                                .where(DSL.lower(ENTITY_ALIAS.ALIAS).in(tokens))
                                .and(lifecycleCondition))
                .limit(options.limit())
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }

}
