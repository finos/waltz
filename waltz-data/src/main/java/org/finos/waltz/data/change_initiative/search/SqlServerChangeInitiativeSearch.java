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

package org.finos.waltz.data.change_initiative.search;

import org.finos.waltz.data.DatabaseVendorSpecific;
import org.finos.waltz.data.FullTextSearch;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;

import java.util.List;

import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;

public class SqlServerChangeInitiativeSearch implements FullTextSearch<ChangeInitiative>, DatabaseVendorSpecific {

    @Override
    public List<ChangeInitiative> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());
        return dsl
                .select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .limit(options.limit())
                .fetch(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
