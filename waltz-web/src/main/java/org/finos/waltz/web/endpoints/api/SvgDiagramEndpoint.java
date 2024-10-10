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

import org.finos.waltz.model.svg.SvgDiagram;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.svg.SvgDiagramService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.WebUtilities.requireRole;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class SvgDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "svg-diagram");

    private final SvgDiagramService svgDiagramService;
    private final UserRoleService userRoleService;


    @Autowired
    public SvgDiagramEndpoint(SvgDiagramService svgDiagramService,
                              UserRoleService userRoleService) {
        this.svgDiagramService = svgDiagramService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);
        String getById = mkPath(BASE_URL, ":id");
        String removePath = mkPath(BASE_URL, ":id");
        String savePath = mkPath(BASE_URL, "save");
        String findByGroupsPath = mkPath(BASE_URL, "group");

        ListRoute<SvgDiagram> findByGroupsRoute = (request, response) ->
                svgDiagramService.findByGroups(request.queryParamsValues("group"));

        ListRoute<SvgDiagram> findAllRoute = (request, response) ->
                svgDiagramService.findAll();

        DatumRoute<SvgDiagram> getByIdRoute = (request, response) -> {
            return svgDiagramService.getById(getId(request));
        };

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.ADMIN);
            return svgDiagramService.remove(getId(request));
        };

        DatumRoute<Boolean> saveRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.ADMIN);
            return svgDiagramService.save(readBody(request, SvgDiagram.class));
        };

        getForList(findByGroupsPath, findByGroupsRoute);
        getForList(findAllPath, findAllRoute);
        getForDatum(getById, getByIdRoute);
        deleteForDatum(removePath, removeRoute);
        postForDatum(savePath, saveRoute);
    }
}
