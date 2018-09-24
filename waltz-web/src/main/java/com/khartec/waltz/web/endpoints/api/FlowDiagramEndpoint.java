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
import static com.khartec.waltz.model.user.Role.LINEAGE_EDITOR;
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
