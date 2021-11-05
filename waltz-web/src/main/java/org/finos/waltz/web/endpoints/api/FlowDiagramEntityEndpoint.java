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

import org.finos.waltz.service.flow_diagram.FlowDiagramEntityService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.flow_diagram.FlowDiagramEntity;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class FlowDiagramEntityEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "flow-diagram-entity");
    private final FlowDiagramEntityService flowDiagramEntityService;
    private final UserRoleService userRoleService;


    @Autowired
    public FlowDiagramEntityEndpoint(FlowDiagramEntityService flowDiagramEntityService, UserRoleService userRoleService) {
        checkNotNull(flowDiagramEntityService, "flowDiagramEntityService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.flowDiagramEntityService = flowDiagramEntityService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {
        String findByDiagramIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id");
        String removeRelationshipPath = WebUtilities.mkPath(findByDiagramIdPath, ":entityKind", ":entityId");
        String addRelationshipPath = WebUtilities.mkPath(findByDiagramIdPath, ":entityKind", ":entityId");
        String findByEntityPath = WebUtilities.mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForSelectorPath = WebUtilities.mkPath(BASE_URL, "selector");

        ListRoute<FlowDiagramEntity> findByDiagramIdRoute = (req, res)
                -> flowDiagramEntityService.findByDiagramId(WebUtilities.getId(req));
        ListRoute<FlowDiagramEntity> findForSelectorRoute = (req, res)
                -> flowDiagramEntityService.findForDiagramSelector(WebUtilities.readIdSelectionOptionsFromBody(req));
        ListRoute<FlowDiagramEntity> findByEntityRoute = (req, res)
                -> flowDiagramEntityService.findByEntityReference(WebUtilities.getEntityReference(req));

        ListRoute<FlowDiagramEntity> addRelationshipRoute = (req, res) -> {
            WebUtilities.requireRole(userRoleService, req, SystemRole.LINEAGE_EDITOR);
            long diagramId = WebUtilities.getId(req);
            flowDiagramEntityService.addRelationship(
                    diagramId,
                    WebUtilities.getEntityReference(req, "entityKind", "entityId"));
            return flowDiagramEntityService.findByDiagramId(diagramId);
        };

        ListRoute<FlowDiagramEntity> removeRelationshipRoute = (req, res) -> {
            WebUtilities.requireRole(userRoleService, req, SystemRole.LINEAGE_EDITOR);
            long diagramId = WebUtilities.getId(req);
            flowDiagramEntityService.removeRelationship(
                    diagramId,
                    WebUtilities.getEntityReference(req, "entityKind", "entityId"));
            return flowDiagramEntityService.findByDiagramId(diagramId);
        };

        EndpointUtilities.getForList(findByDiagramIdPath, findByDiagramIdRoute);
        EndpointUtilities.getForList(findByEntityPath, findByEntityRoute);
        EndpointUtilities.postForList(findForSelectorPath, findForSelectorRoute);

        EndpointUtilities.postForList(addRelationshipPath, addRelationshipRoute);
        EndpointUtilities.deleteForList(removeRelationshipPath, removeRelationshipRoute);
    }

}
