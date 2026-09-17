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
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.database_information.DatabaseInformationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.database_information.DatabaseInformation;
import org.finos.waltz.model.database_information.DatabaseSummaryStatistics;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseInformationService {

    private final DatabaseInformationDao databaseInformationDao;
    private final ChangeLogService changeLogService;
    private final ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();

    @Autowired
    public DatabaseInformationService(DatabaseInformationDao databaseInformationDao,
                                      ChangeLogService changeLogService) {
        Checks.checkNotNull(databaseInformationDao, "databaseInformationDao cannot be null");
        Checks.checkNotNull(changeLogService, "changeLogService cannot be null");
        this.databaseInformationDao = databaseInformationDao;
        this.changeLogService = changeLogService;
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


    /**
     * Upserts a batch of database assets (keyed on external id) and records a change-log entry
     * against each. Databases are part of the asset inventory; associating them with an
     * application is a separate concern handled via {@link org.finos.waltz.service.database_usage.DatabaseUsageService}.
     */
    public Collection<DatabaseInformation> bulkSave(List<DatabaseInformation> databases, String username) {
        checkNotNull(databases, "databases cannot be null");
        checkNotNull(username, "username cannot be null");
        if (databases.stream().anyMatch(d -> !d.externalId().isPresent())) {
            throw new IllegalArgumentException("every database must have an externalId (used as the upsert key)");
        }

        Collection<DatabaseInformation> saved = databaseInformationDao.bulkUpsert(databases);

        Collection<ChangeLog> logs = saved
                .stream()
                .map(d -> ImmutableChangeLog.builder()
                        .message(format("Bulk saved database: %s", d.databaseName()))
                        .parentReference(d.entityReference())
                        .userId(username)
                        .createdAt(DateTimeUtilities.nowUtc())
                        .severity(Severity.INFORMATION)
                        .childKind(EntityKind.DATABASE)
                        .operation(Operation.UPDATE)
                        .build())
                .collect(Collectors.toList());
        changeLogService.write(logs);

        return saved;
    }
}
