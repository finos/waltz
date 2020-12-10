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

package com.khartec.waltz.service.database_information;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.database_information.DatabaseInformationDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseInformationService {

    private final DatabaseInformationDao databaseInformationDao;
    private final ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();

    @Autowired
    public DatabaseInformationService(DatabaseInformationDao databaseInformationDao) {
        Checks.checkNotNull(databaseInformationDao, "databaseInformationDao cannot be null");
        this.databaseInformationDao = databaseInformationDao;
    }

    public List<DatabaseInformation> findByApplicationId(Long id) {
        checkNotNull(id, "id cannot be null");
        return databaseInformationDao.findByApplicationId(id);
    }

    public Map<Long, List<DatabaseInformation>> findByApplicationSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        return databaseInformationDao.findByAppSelector(factory.apply(options));
    }

    public DatabaseSummaryStatistics calculateStatsForAppIdSelector(IdSelectionOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        return databaseInformationDao.calculateStatsForAppSelector(factory.apply(options));
    }
        
}
