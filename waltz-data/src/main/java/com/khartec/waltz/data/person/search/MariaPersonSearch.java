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

package com.khartec.waltz.data.person.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.person.Person;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

public class MariaPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {


    private static final String QUERY
            = "SELECT * FROM person\n"
            + " WHERE\n"
            + " MATCH(display_name, email, user_principal_name)\n"
            + " AGAINST (?)\n"
            + " LIMIT ?";


    @Override
    public List<Person> search(DSLContext dsl, String terms, EntitySearchOptions options) {
        Result<Record> records = dsl.fetch(QUERY, terms, options.limit());
        return records.map(PersonDao.personMapper);
    }

}
