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

import com.khartec.waltz.model.UpdateDescriptionCommand;
import com.khartec.waltz.model.UpdateNameCommand;
import com.khartec.waltz.model.flow_diagram.FlowDiagram;
import com.khartec.waltz.model.flow_diagram.SaveDiagramCommand;
import com.khartec.waltz.service.flow_diagram.FlowDiagramService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.user.SystemRole.LINEAGE_EDITOR;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class FlowDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "flow-diagram");
    private final FlowDiagramService flowDiagramService;
    private final UserRoleService userRoleService;


    @Autowired
    public FlowDiagramEndpoint(FlowDiagramService flowDiagramService,
                               UserRoleService userRoleService) {
        checkNotNull(flowDiagramService, "flowDiagramService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.flowDiagramService = flowDiagramService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String diagramIdPath = mkPath(BASE_URL, "id", ":id");
        String entityIdPath = mkPath(BASE_URL, "entity", ":kind", ":id");

        String getByIdPath = diagramIdPath;
        String deleteByIdPath = diagramIdPath;
        String findByEntityPath = entityIdPath;
        String makeNewDiagramPath = entityIdPath;
        String findForSelectorPath = mkPath(BASE_URL, "selector");
        String saveDiagramPath = BASE_URL;
        String updateNamePath = mkPath(BASE_URL, "update-name", ":id");
        String updateDescriptionPath = mkPath(BASE_URL, "update-description", ":id");
        String cloneDiagramPath = mkPath(diagramIdPath, "clone");

        DatumRoute<FlowDiagram> getByIdRoute = (req, res)
                -> flowDiagramService.getById(getId(req));
        ListRoute<FlowDiagram> findByEntityRoute = (req, res)
                -> flowDiagramService.findByEntityReference(getEntityReference(req));
        ListRoute<FlowDiagram> findForSelectorRoute = (req, res)
                -> flowDiagramService.findForSelector(readIdSelectionOptionsFromBody(req));
        DatumRoute<Long> saveDiagramRoute = (req, res)
                ->  {
            requireRole(userRoleService, req, LINEAGE_EDITOR);
            return flowDiagramService.save(
                    readBody(req, SaveDiagramCommand.class),
                    getUsername(req));
        };

        DatumRoute<Boolean> updateNameRoute = (req, res)
                ->  {
            requireRole(userRoleService, req, LINEAGE_EDITOR);
            return flowDiagramService.updateName(
                    getId(req),
                    readBody(req, UpdateNameCommand.class),
                    getUsername(req));
        };

        DatumRoute<Boolean> updateDescriptionRoute = (req, res)
                ->  {
            requireRole(userRoleService, req, LINEAGE_EDITOR);
            return flowDiagramService.updateDescription(
                    getId(req),
                    readBody(req, UpdateDescriptionCommand.class),
                    getUsername(req));
        };

        DatumRoute<Long> cloneDiagramRoute = (req, res) -> {
            requireRole(userRoleService, req, LINEAGE_EDITOR);
            return flowDiagramService
                    .cloneDiagram(
                            getId(req),
                            req.body(),
                            getUsername(req));
        };

        DatumRoute<Long> makeNewDiagramRoute = (req, res) -> {
            requireRole(userRoleService, req, LINEAGE_EDITOR);
            return flowDiagramService
                    .makeNewDiagramForEntity(
                            getEntityReference(req),
                            getUsername(req),
                            req.body());
        };

        DatumRoute<Boolean> deleteByIdRoute = (req, res)
                -> flowDiagramService.deleteById(getId(req), getUsername(req));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByEntityPath, findByEntityRoute);

        postForList(findForSelectorPath, findForSelectorRoute);

        postForDatum(saveDiagramPath, saveDiagramRoute);
        postForDatum(updateNamePath, updateNameRoute);
        postForDatum(updateDescriptionPath, updateDescriptionRoute);
        postForDatum(cloneDiagramPath, cloneDiagramRoute);
        postForDatum(makeNewDiagramPath, makeNewDiagramRoute);

        deleteForDatum(deleteByIdPath, deleteByIdRoute);
    }

}
