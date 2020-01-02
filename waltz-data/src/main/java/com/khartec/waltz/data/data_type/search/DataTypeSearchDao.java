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
import static java.util.stream.Collectors.toList;


@Repository
public class DataTypeSearchDao {

    private final DataTypeDao dataTypeDao;


    @Autowired
    public DataTypeSearchDao(DataTypeDao dataTypeDao) {
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");

        this.dataTypeDao = dataTypeDao;
    }


    public List<DataType> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");

        List<String> terms = SearchUtilities.mkTerms(options.searchQuery().toLowerCase());
        return dataTypeDao.findAll()
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
