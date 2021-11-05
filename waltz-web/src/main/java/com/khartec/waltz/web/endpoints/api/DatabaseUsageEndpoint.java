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

package com.khartec.waltz.web.endpoints.api;

import org.finos.waltz.service.database_usage.DatabaseUsageService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.database_usage.DatabaseUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class DatabaseUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "database-usage");

    private final DatabaseUsageService databaseUsageService;

    @Autowired
    public DatabaseUsageEndpoint(DatabaseUsageService databaseUsageService) {
        this.databaseUsageService = databaseUsageService;
    }

    @Override
    public void register() {

        String findByReferencedEntityPath = mkPath(BASE_URL, "ref", ":kind", ":id");
        String findByDatabaseIdPath = mkPath(BASE_URL, "database-id", ":id");

        ListRoute<DatabaseUsage> findByReferencedEntityRoute = (request, response)
                -> databaseUsageService.findByEntityReference(getEntityReference(request));

        ListRoute<DatabaseUsage> findByDatabaseIdRoute = (request, response)
                -> databaseUsageService.findByDatabaseId(getId(request));

        getForList(findByReferencedEntityPath, findByReferencedEntityRoute);
        getForList(findByDatabaseIdPath, findByDatabaseIdRoute);
    }

}
