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

import com.khartec.waltz.model.process_diagram.ProcessDiagram;
import com.khartec.waltz.model.process_diagram.ProcessDiagramAndEntities;
import com.khartec.waltz.service.process_diagram.ProcessDiagramService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class ProcessDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "process-diagram");
    private final ProcessDiagramService diagramService;


    @Autowired
    public ProcessDiagramEndpoint(ProcessDiagramService diagramService) {
        this.diagramService = diagramService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String getByExternalIdPath = mkPath(BASE_URL, "external-id", ":externalId");
        String findForSelectorPath = mkPath(BASE_URL, "selector", ":kind");

        DatumRoute<ProcessDiagramAndEntities> getByIdRoute = (req, res)
                -> diagramService.getDiagramAndEntitiesById(getId(req));

        DatumRoute<ProcessDiagram> getByExternalIdRoute = (req, res)
                -> diagramService.getByExternalId(req.params("externalId"));

        ListRoute<ProcessDiagram> findForSelectorRoute = (req, res)
                -> diagramService.findByGenericSelector(
                        getKind(req),
                        readIdSelectionOptionsFromBody(req));

        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getByExternalIdPath, getByExternalIdRoute);
        postForList(findForSelectorPath, findForSelectorRoute);
    }

}
