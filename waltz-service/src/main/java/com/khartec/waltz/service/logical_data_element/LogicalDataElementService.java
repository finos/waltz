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
