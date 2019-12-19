/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.service.logical_data_element;

import com.khartec.waltz.data.logical_data_element.LogicalDataElementDao;
import com.khartec.waltz.data.logical_data_element.LogicalDataElementIdSelectorFactory;
import com.khartec.waltz.data.logical_data_element.search.LogicalDataElementSearchDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class LogicalDataElementService {
    
    private final LogicalDataElementDao logicalDataElementDao;
    private final LogicalDataElementSearchDao logicalDataElementSearchDao;
    private final LogicalDataElementIdSelectorFactory idSelectorFactory = new LogicalDataElementIdSelectorFactory();


    public LogicalDataElementService(LogicalDataElementDao logicalDataElementDao,
                                     LogicalDataElementSearchDao logicalDataElementSearchDao) {
        checkNotNull(logicalDataElementDao, "logicalDataElementDao cannot be null");
        checkNotNull(logicalDataElementSearchDao, "logicalDataElementSearchDao cannot be null");

        this.logicalDataElementDao = logicalDataElementDao;
        this.logicalDataElementSearchDao = logicalDataElementSearchDao;
    }


    public LogicalDataElement getById(long id) {
        return logicalDataElementDao.getById(id);
    }


    public LogicalDataElement getByExternalId(String externalId) {
        return logicalDataElementDao.getByExternalId(externalId);
    }


    public List<LogicalDataElement> findAll() {
        return logicalDataElementDao.findAll();
    }


    public List<LogicalDataElement> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return logicalDataElementDao.findBySelector(selector);
    }


    public List<LogicalDataElement> search(EntitySearchOptions options) {
        return logicalDataElementSearchDao.search(options);
    }

}
