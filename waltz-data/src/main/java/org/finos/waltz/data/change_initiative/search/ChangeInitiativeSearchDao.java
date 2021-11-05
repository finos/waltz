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

import org.finos.waltz.data.*;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.SetUtilities.orderedUnion;

@Repository
public class ChangeInitiativeSearchDao implements SearchDao<ChangeInitiative> {


    private final DSLContext dsl;
    private final FullTextSearch<ChangeInitiative> searcher;


    @Autowired
    public ChangeInitiativeSearchDao(DSLContext dsl) {
        this.dsl = dsl;
        this.searcher = determineSearcher(dsl.dialect());
    }


    @Override
    public List<ChangeInitiative> search(EntitySearchOptions options) {
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return emptyList();
        }

        Condition nameCondition = JooqUtilities.mkBasicTermSearch(CHANGE_INITIATIVE.NAME, terms);

        List<ChangeInitiative> ciViaName = dsl
                .select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(nameCondition)
                .orderBy(CHANGE_INITIATIVE.NAME)
                .limit(options.limit())
                .fetch(ChangeInitiativeDao.TO_DOMAIN_MAPPER);

        List<ChangeInitiative> ciViaFullText = searcher.searchFullText(dsl, options);
        return new ArrayList<>(orderedUnion(ciViaName, ciViaFullText));
    }


    private FullTextSearch<ChangeInitiative> determineSearcher(SQLDialect dialect) {

        if (JooqUtilities.isPostgres(dialect)) {
            return new PostgresChangeInitiativeSearch();
        }

        if (JooqUtilities.isMariaDB(dialect)) {
            return new MariaChangeInitiativeSearch();
        }

        if (JooqUtilities.isSQLServer(dialect)) {
            return new SqlServerChangeInitiativeSearch();
        }

        return new UnsupportedSearcher<>(dialect);
    }
}
