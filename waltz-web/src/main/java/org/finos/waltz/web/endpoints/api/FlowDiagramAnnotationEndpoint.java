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

import org.finos.waltz.service.flow_diagram.FlowDiagramAnnotationService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.flow_diagram.FlowDiagramAnnotation;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class FlowDiagramAnnotationEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "flow-diagram-annotation");
    private final FlowDiagramAnnotationService flowDiagramAnnotationService;


    @Autowired
    public FlowDiagramAnnotationEndpoint(FlowDiagramAnnotationService flowDiagramAnnotationService) {
        checkNotNull(flowDiagramAnnotationService, "flowDiagramAnnotationService cannot be null");
        this.flowDiagramAnnotationService = flowDiagramAnnotationService;
    }


    @Override
    public void register() {
        String findByDiagramPath = WebUtilities.mkPath(BASE_URL, "diagram", ":id");

        ListRoute<FlowDiagramAnnotation> findByDiagramRoute = (req, res)
                -> flowDiagramAnnotationService.findByDiagramId(WebUtilities.getId(req));

        EndpointUtilities.getForList(findByDiagramPath, findByDiagramRoute);
    }

}
