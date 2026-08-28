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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.service.database_usage.DatabaseUsageService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.database_usage.DatabaseUsage;
import org.finos.waltz.model.database_usage.DatabaseUsageCreateCommand;
import org.finos.waltz.model.user.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class DatabaseUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "database-usage");

    private final DatabaseUsageService databaseUsageService;
    private final UserRoleService userRoleService;

    @Autowired
    public DatabaseUsageEndpoint(DatabaseUsageService databaseUsageService,
                                 UserRoleService userRoleService) {
        checkNotNull(databaseUsageService, "databaseUsageService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.databaseUsageService = databaseUsageService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        String findByReferencedEntityPath = mkPath(BASE_URL, "ref", ":kind", ":id");
        String findByDatabaseIdPath = mkPath(BASE_URL, "database-id", ":id");

        ListRoute<DatabaseUsage> findByReferencedEntityRoute = (request, response)
                -> databaseUsageService.findByEntityReference(getEntityReference(request));

        ListRoute<DatabaseUsage> findByDatabaseIdRoute = (request, response)
                -> databaseUsageService.findByDatabaseId(getId(request));

        DatumRoute<Collection<DatabaseUsage>> addUsagesRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.ADMIN);
            String username = getUsername(request);
            List<DatabaseUsageCreateCommand> commands = readList(request, DatabaseUsageCreateCommand.class);
            return databaseUsageService.addUsages(getEntityReference(request), commands, username);
        };

        getForList(findByReferencedEntityPath, findByReferencedEntityRoute);
        getForList(findByDatabaseIdPath, findByDatabaseIdRoute);
        postForDatum(findByReferencedEntityPath, addUsagesRoute);
    }

}
