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

import org.finos.waltz.service.process_diagram.ProcessDiagramEntityService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.process_diagram.ProcessDiagramEntityApplicationAlignment;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessDiagramEntityEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "process-diagram-entity");
    private final ProcessDiagramEntityService processDiagramEntityService;


    @Autowired
    public ProcessDiagramEntityEndpoint(ProcessDiagramEntityService processDiagramEntityService) {
        this.processDiagramEntityService = processDiagramEntityService;
    }


    @Override
    public void register() {
        String findApplicationAlignmentsByDiagramIdPath = WebUtilities.mkPath(BASE_URL, "diagram-id", ":id", "app-alignments");

        ListRoute<ProcessDiagramEntityApplicationAlignment> findApplicationAlignmentsByDiagramIdRoute = (req, res)
                -> processDiagramEntityService.findApplicationAlignmentsByDiagramId(WebUtilities.getId(req));

        EndpointUtilities.getForList(findApplicationAlignmentsByDiagramIdPath, findApplicationAlignmentsByDiagramIdRoute);
    }
}
