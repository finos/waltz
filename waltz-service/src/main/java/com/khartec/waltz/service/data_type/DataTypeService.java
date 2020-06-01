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

package com.khartec.waltz.service.data_type;

import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.data_type.search.DataTypeSearchDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DataTypeService {


    private final DataTypeDao dataTypeDao;
    private final DataTypeSearchDao searchDao;


    @Autowired
    public DataTypeService(DataTypeDao dataTypeDao, DataTypeSearchDao searchDao) {
        checkNotNull(dataTypeDao, "dataTypeDao must not be null");
        checkNotNull(searchDao, "searchDao cannot be null");

        this.dataTypeDao = dataTypeDao;
        this.searchDao = searchDao;
    }


    public List<DataType> findAll() {
        return dataTypeDao.findAll();
    }


    public DataType getDataTypeById(long dataTypeId) {
        return dataTypeDao.getById(dataTypeId);
    }


    public DataType getDataTypeByCode(String code) {
        return dataTypeDao.getByCode(code);
    }

    public List<EntityReference> findByIdSelector(Select<Record1<Long>> selector) {
        return dataTypeDao.findByIdSelectorAsEntityReference(selector);
    }


    public Collection<DataType> search(EntitySearchOptions options) {
        return searchDao.search(options);
    }

    /**
     * Attempts to return the datatype that has been declared as unknown (if one exists)
     * @return `Optional.of(unknownDataType)` if an unknown datatype has been defined otherwise `Optional.empty()`.
     */
    public Optional<DataType> getUnknownDataType() {
        return dataTypeDao
                .findAll()
                .stream()
                .filter(DataType::unknown)
                .findFirst();
    }
}
