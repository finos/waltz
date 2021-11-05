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

import org.finos.waltz.service.svg.SvgDiagramService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.svg.SvgDiagram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class SvgDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "svg-diagram");

    private final SvgDiagramService svgDiagramService;


    @Autowired
    public SvgDiagramEndpoint(SvgDiagramService svgDiagramService) {
        this.svgDiagramService = svgDiagramService;
    }


    @Override
    public void register() {
        String findByGroupsPath = mkPath(BASE_URL, "group");

        ListRoute<SvgDiagram> findByGroupsRoute = (request, response) ->
                svgDiagramService.findByGroups(request.queryParamsValues("group"));

        getForList(findByGroupsPath, findByGroupsRoute);
    }
}
