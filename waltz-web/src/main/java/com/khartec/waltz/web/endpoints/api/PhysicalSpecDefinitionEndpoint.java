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

import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionChangeCommand;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalSpecDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition");

    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionService specDefinitionService;


    @Autowired
    public PhysicalSpecDefinitionEndpoint(UserRoleService userRoleService,
                                          PhysicalSpecDefinitionService specDefinitionService) {
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionService, "specDefinitionService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionService = specDefinitionService;
    }


    @Override
    public void register() {
        String findForSpecificationPath = mkPath(BASE_URL, "specification", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String createPath = mkPath(BASE_URL, "specification", ":id");
        String updateStatusPath = mkPath(BASE_URL, "specification", ":id", "status");
        String deletePath = mkPath(BASE_URL, "specification", ":id");

        ListRoute<PhysicalSpecDefinition> findForSpecificationRoute =
                (req, res) -> specDefinitionService.findForSpecification(getId(req));

        ListRoute<PhysicalSpecDefinition> findBySelectorRoute =
                (req, res) -> specDefinitionService.findBySelector(readIdSelectionOptionsFromBody(req));


        DatumRoute<Long> createRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.create(
                    getUsername(req),
                    getId(req),
                    readBody(req, PhysicalSpecDefinitionChangeCommand.class));
        };

        DatumRoute<Boolean> updateStatusRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.updateStatus(
                    getUsername(req),
                    getId(req),
                    readBody(req, ReleaseLifecycleStatusChangeCommand.class));
        };

        DatumRoute<Integer> deleteRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.delete(
                    getUsername(req),
                    getId(req));
        };


        getForList(findForSpecificationPath, findForSpecificationRoute);
        postForList(findBySelectorPath, findBySelectorRoute);

        postForDatum(createPath, createRoute);
        putForDatum(updateStatusPath, updateStatusRoute);
        deleteForDatum(deletePath, deleteRoute);
    }
}
