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

package com.khartec.waltz.data;

import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.emptyList;

public class UnsupportedSearcher<T> implements FullTextSearch<T> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsupportedSearcher.class);

    private final SQLDialect dialect;


    public UnsupportedSearcher(SQLDialect dialect) {
        this.dialect = dialect;
    }


    @Override
    public List<T> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        LOG.error("Search not supported/implemented for database dialect: " + dialect);
        return emptyList();
    }

}
