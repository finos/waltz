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

package com.khartec.waltz.data.data_type.search;

import com.khartec.waltz.data.SearchUtilities;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.PredicateUtilities.all;
import static com.khartec.waltz.common.StringUtilities.length;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;


@Repository
public class DataTypeSearchDao {

    private final DataTypeDao dataTypeDao;


    @Autowired
    public DataTypeSearchDao(DataTypeDao dataTypeDao) {
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");

        this.dataTypeDao = dataTypeDao;
    }


    public List<DataType> search(String query, EntitySearchOptions options) {
        checkNotNull(query, "query cannot be null");
        checkNotNull(options, "options cannot be null");

        if (length(query) < 3) {
            return emptyList();
        }

        List<String> terms = SearchUtilities.mkTerms(query.toLowerCase());
        return dataTypeDao.getAll()
                .stream()
                .filter(dataType -> {
                    String s = (dataType.name() + " " + dataType.description()).toLowerCase();
                    return all(
                            terms,
                            t -> s.indexOf(t) > -1);
                })
                .limit(options.limit())
                .collect(toList());
    }

}
