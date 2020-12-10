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

import com.khartec.waltz.model.staticpanel.StaticPanel;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.static_panel.StaticPanelService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class StaticPanelEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "static-panel");


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
        String findByGroupsPath = mkPath(BASE_URL, "group");
        String findAllPath = BASE_URL;

        ListRoute<StaticPanel> findByGroupsRoute = (request, response) ->
                staticPanelService.findByGroups(request.queryParamsValues("group"));

        ListRoute<StaticPanel> findAllRoute = (request, response) ->
                staticPanelService.findAll();

        getForList(findByGroupsPath, findByGroupsRoute);
        getForList(findAllPath, findAllRoute);

        postForDatum(BASE_URL, this::saveRoute);
    }


    private boolean saveRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.ADMIN);
        StaticPanel panel = readBody(request, StaticPanel.class);
        return staticPanelService.save(panel);
    }


}
