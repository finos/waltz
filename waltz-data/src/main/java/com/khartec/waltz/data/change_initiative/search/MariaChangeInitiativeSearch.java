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

package com.khartec.waltz.data.change_initiative.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaChangeInitiativeSearch implements FullTextSearch<ChangeInitiative>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM change_initiative\n"
            + " WHERE\n"
            + "  MATCH(name, description, external_id)\n"
            + "  AGAINST (?)\n"
            + " LIMIT ?";

    @Override
    public List<ChangeInitiative> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        Result<Record> records = dsl.fetch(QUERY, options.searchQuery(), options.limit());
        return records.map(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
