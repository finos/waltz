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

package com.khartec.waltz.service.data_type;

import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.data_type.search.DataTypeSearchDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
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


    public List<DataType> getAll() {
        return dataTypeDao.getAll();
    }


    public Collection<DataType> search(String query) {
        return searchDao.search(query, EntitySearchOptions.mkForEntity(EntityKind.DATA_TYPE));
    }

    /**
     * Attempts to return the datatype that has been declared as unknown (if one exists)
     * @return `Optional.of(unknownDataType)` if an unknown datatype has been defined otherwise `Optional.empty()`.
     */
    public Optional<DataType> getUnknownDataType() {
        return dataTypeDao
                .getAll()
                .stream()
                .filter(DataType::unknown)
                .findFirst();
    }
}
