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

import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.model.user.Role;
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
                -> flowDiagramEntityService.findForSelector(readIdSelectionOptionsFromBody(req));
        ListRoute<FlowDiagramEntity> findByEntityRoute = (req, res)
                -> flowDiagramEntityService.findByEntityReference(getEntityReference(req));

        ListRoute<FlowDiagramEntity> addRelationshipRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LINEAGE_EDITOR);
            long diagramId = getId(req);
            flowDiagramEntityService.addRelationship(
                    diagramId,
                    getEntityReference(req, "entityKind", "entityId"));
            return flowDiagramEntityService.findByDiagramId(diagramId);
        };

        ListRoute<FlowDiagramEntity> removeRelationshipRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LINEAGE_EDITOR);
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
