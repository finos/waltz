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

import org.finos.waltz.service.static_panel.StaticPanelService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.staticpanel.StaticPanel;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class StaticPanelEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "static-panel");


    private final StaticPanelService staticPanelService;
    private final UserRoleService userRoleService;



    @Autowired
    public StaticPanelEndpoint(StaticPanelService staticPanelService, UserRoleService userRoleService) {
        checkNotNull(staticPanelService, "staticPanelService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.staticPanelService = staticPanelService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByGroupsPath = WebUtilities.mkPath(BASE_URL, "group");
        String findAllPath = BASE_URL;

        ListRoute<StaticPanel> findByGroupsRoute = (request, response) ->
                staticPanelService.findByGroups(request.queryParamsValues("group"));

        ListRoute<StaticPanel> findAllRoute = (request, response) ->
                staticPanelService.findAll();

        EndpointUtilities.getForList(findByGroupsPath, findByGroupsRoute);
        EndpointUtilities.getForList(findAllPath, findAllRoute);

        EndpointUtilities.postForDatum(BASE_URL, this::saveRoute);
    }


    private boolean saveRoute(Request request, Response response) throws IOException {
        WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
        StaticPanel panel = WebUtilities.readBody(request, StaticPanel.class);
        return staticPanelService.save(panel);
    }


}
