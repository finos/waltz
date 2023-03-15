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

package org.finos.waltz.service.database_information;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.database_information.DatabaseInformationDao;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.database_information.DatabaseInformation;
import org.finos.waltz.model.database_information.DatabaseSummaryStatistics;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.finos.waltz.common.Checks.checkNotNull;

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

    public DatabaseInformation getById(long id) {
        checkNotNull(id, "id cannot be null");
        return databaseInformationDao.getById(id);
    }

    public DatabaseInformation getByExternalId(String externalId) {
        checkNotNull(externalId, " external id cannot be null");
        return databaseInformationDao.getByExternalId(externalId);
    }

    public List<DatabaseInformation> search(EntitySearchOptions options) {
        return databaseInformationDao.search(options);
    }

    public Long createDatabase(DatabaseInformation info) {
        return databaseInformationDao.createDatabase(info);
    }
}
