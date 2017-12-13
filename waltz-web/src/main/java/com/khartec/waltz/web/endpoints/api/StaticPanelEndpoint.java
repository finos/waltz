/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.staticpanel.StaticPanel;
import com.khartec.waltz.model.user.Role;
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
        requireRole(userRoleService, request, Role.ADMIN);
        StaticPanel panel = readBody(request, StaticPanel.class);
        return staticPanelService.save(panel);
    }


}
