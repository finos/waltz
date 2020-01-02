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

import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.flow_diagram.FlowDiagramEntityService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class FlowDiagramEntityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "flow-diagram-entity");
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
        String findByDiagramIdPath = mkPath(BASE_URL, "id", ":id");
        String removeRelationshipPath = mkPath(findByDiagramIdPath, ":entityKind", ":entityId");
        String addRelationshipPath = mkPath(findByDiagramIdPath, ":entityKind", ":entityId");
        String findByEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findForSelectorPath = mkPath(BASE_URL, "selector");

        ListRoute<FlowDiagramEntity> findByDiagramIdRoute = (req, res)
                -> flowDiagramEntityService.findByDiagramId(getId(req));
        ListRoute<FlowDiagramEntity> findForSelectorRoute = (req, res)
                -> flowDiagramEntityService.findForDiagramSelector(readIdSelectionOptionsFromBody(req));
        ListRoute<FlowDiagramEntity> findByEntityRoute = (req, res)
                -> flowDiagramEntityService.findByEntityReference(getEntityReference(req));

        ListRoute<FlowDiagramEntity> addRelationshipRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LINEAGE_EDITOR);
            long diagramId = getId(req);
            flowDiagramEntityService.addRelationship(
                    diagramId,
                    getEntityReference(req, "entityKind", "entityId"));
            return flowDiagramEntityService.findByDiagramId(diagramId);
        };

        ListRoute<FlowDiagramEntity> removeRelationshipRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LINEAGE_EDITOR);
            long diagramId = getId(req);
            flowDiagramEntityService.removeRelationship(
                    diagramId,
                    getEntityReference(req, "entityKind", "entityId"));
            return flowDiagramEntityService.findByDiagramId(diagramId);
        };

        getForList(findByDiagramIdPath, findByDiagramIdRoute);
        getForList(findByEntityPath, findByEntityRoute);
        postForList(findForSelectorPath, findForSelectorRoute);

        postForList(addRelationshipPath, addRelationshipRoute);
        deleteForList(removeRelationshipPath, removeRelationshipRoute);
    }

}
