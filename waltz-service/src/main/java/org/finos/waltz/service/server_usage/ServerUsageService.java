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

package org.finos.waltz.service.server_usage;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.server_usage.ServerUsageDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.server_usage.ServerUsage;
import org.finos.waltz.model.server_usage.ServerUsageCreateCommand;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class ServerUsageService {

    private final ServerUsageDao serverUsageDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public ServerUsageService(ServerUsageDao serverUsageDao,
                              ChangeLogService changeLogService) {
        checkNotNull(serverUsageDao, "serverUsageDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.serverUsageDao = serverUsageDao;
        this.changeLogService = changeLogService;
    }


    public Collection<ServerUsage> findByServerId(long serverId) {
        return serverUsageDao.findByServerId(serverId);
    }


    public Collection<ServerUsage> findByReferencedEntity(EntityReference ref) {
        return serverUsageDao.findByReferencedEntity(ref);
    }


    /**
     * Links the given server assets to an entity (e.g. an application) in the requested
     * environments, records a change-log entry against that entity, and returns the entity's
     * resulting usages.
     */
    public Collection<ServerUsage> addUsages(EntityReference ref,
                                             Collection<ServerUsageCreateCommand> commands,
                                             String username) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(commands, "commands cannot be null");
        checkNotNull(username, "username cannot be null");

        int linkCount = serverUsageDao.create(ref, commands, username);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(format("Linked %d server(s)", linkCount))
                .parentReference(ref)
                .userId(username)
                .createdAt(DateTimeUtilities.nowUtc())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.SERVER)
                .operation(Operation.ADD)
                .build());

        return serverUsageDao.findByReferencedEntity(ref);
    }
}
