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
