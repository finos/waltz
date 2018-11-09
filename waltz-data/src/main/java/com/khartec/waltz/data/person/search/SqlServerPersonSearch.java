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
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.person.Person;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Person.PERSON;

public class SqlServerPersonSearch implements FullTextSearch<Person>, DatabaseVendorSpecific {

    @Override
    public List<Person> search(DSLContext dsl, String query, EntitySearchOptions options) {
        List<String> terms = mkTerms(query);
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        boolean showRemoved = options
                .entityLifecycleStatuses()
                .contains(EntityLifecycleStatus.REMOVED);

        Condition maybeFilterRemoved = showRemoved
            ? DSL.trueCondition()  // match anything
            : PERSON.IS_REMOVED.isFalse();

        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .and(maybeFilterRemoved)
                .limit(options.limit())
                .fetch(PersonDao.personMapper);
    }
}
