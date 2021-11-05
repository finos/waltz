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

import org.finos.waltz.service.server_usage.ServerUsageService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.server_usage.ServerUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class ServerUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "server-usage");

    private final ServerUsageService serverUsageService;


    @Autowired
    public ServerUsageEndpoint(ServerUsageService serverUsageService) {
        checkNotNull(serverUsageService, "serverUsageService cannot be null");
        this.serverUsageService = serverUsageService;
    }


    @Override
    public void register() {

        String findByReferencedEntityPath = mkPath(BASE_URL, "ref", ":kind", ":id");
        String findByServerIdPath = mkPath(BASE_URL, "server-id", ":id");

        ListRoute<ServerUsage> findByReferencedEntityRoute = (request, response)
                -> serverUsageService.findByReferencedEntity(getEntityReference(request));

        ListRoute<ServerUsage> findByServerIdRoute = (request, response)
                -> serverUsageService.findByServerId(getId(request));

        getForList(findByReferencedEntityPath, findByReferencedEntityRoute);
        getForList(findByServerIdPath, findByServerIdRoute);
    }

}
