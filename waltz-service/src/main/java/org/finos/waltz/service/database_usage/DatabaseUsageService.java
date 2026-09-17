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

package org.finos.waltz.service.database_usage;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.database_usage.DatabaseUsageDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.database_usage.DatabaseUsage;
import org.finos.waltz.model.database_usage.DatabaseUsageCreateCommand;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseUsageService {

    private final DatabaseUsageDao databaseUsageDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public DatabaseUsageService(DatabaseUsageDao databaseUsageDao,
                                ChangeLogService changeLogService) {
        checkNotNull(databaseUsageDao, "databaseUsageDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.databaseUsageDao = databaseUsageDao;
        this.changeLogService = changeLogService;
    }


    public Collection<DatabaseUsage> findByDatabaseId(long databaseId) {
        return databaseUsageDao.findByDatabaseId(databaseId);
    }


    public Collection<DatabaseUsage> findByEntityReference(EntityReference ref) {
        return databaseUsageDao.findByEntityReference(ref);
    }


    /**
     * Links the given database assets to an entity (e.g. an application) in the requested
     * environments, records a change-log entry against that entity, and returns the entity's
     * resulting usages.
     */
    public Collection<DatabaseUsage> addUsages(EntityReference ref,
                                               Collection<DatabaseUsageCreateCommand> commands,
                                               String username) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(commands, "commands cannot be null");
        checkNotNull(username, "username cannot be null");

        int linkCount = databaseUsageDao.create(ref, commands, username);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(format("Linked %d database(s)", linkCount))
                .parentReference(ref)
                .userId(username)
                .createdAt(DateTimeUtilities.nowUtc())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.DATABASE)
                .operation(Operation.ADD)
                .build());

        return databaseUsageDao.findByEntityReference(ref);
    }
}
