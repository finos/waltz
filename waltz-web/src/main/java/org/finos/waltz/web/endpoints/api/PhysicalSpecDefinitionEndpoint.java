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

import org.finos.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.common.exception.DuplicateKeyException;
import org.finos.waltz.model.ReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionChangeCommand;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;

@Service
public class PhysicalSpecDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "physical-spec-definition");

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
        String findForSpecificationPath = WebUtilities.mkPath(BASE_URL, "specification", ":id");
        String findBySelectorPath = WebUtilities.mkPath(BASE_URL, "selector");
        String createPath = WebUtilities.mkPath(BASE_URL, "specification", ":id");
        String updateStatusPath = WebUtilities.mkPath(BASE_URL, "specification", ":id", "status");
        String deletePath = WebUtilities.mkPath(BASE_URL, "specification", ":id");

        ListRoute<PhysicalSpecDefinition> findForSpecificationRoute =
                (req, res) -> specDefinitionService.findForSpecification(WebUtilities.getId(req));

        ListRoute<PhysicalSpecDefinition> findBySelectorRoute =
                (req, res) -> specDefinitionService.findBySelector(WebUtilities.readIdSelectionOptionsFromBody(req));


        DatumRoute<Long> createRoute = (req, res) -> {
            WebUtilities.requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);


            PhysicalSpecDefinitionChangeCommand physicalSpecDefinitionChangeCommand = WebUtilities.readBody(req, PhysicalSpecDefinitionChangeCommand.class);

            long physicalSpecificationId = WebUtilities.getId(req);

            Set<String> existingVersions = map(specDefinitionService.findForSpecification(physicalSpecificationId), d -> d.version());

            if (existingVersions.contains(physicalSpecDefinitionChangeCommand.version())) {
                throw new DuplicateKeyException("Cannot create physical specification definition with version that already exists");
            }

            return specDefinitionService.create(
                        WebUtilities.getUsername(req),
                        physicalSpecificationId,
                        physicalSpecDefinitionChangeCommand);

        };

        DatumRoute<Boolean> updateStatusRoute = (req, res) -> {
            WebUtilities.requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.updateStatus(
                    WebUtilities.getUsername(req),
                    WebUtilities.getId(req),
                    WebUtilities.readBody(req, ReleaseLifecycleStatusChangeCommand.class));
        };

        DatumRoute<Integer> deleteRoute = (req, res) -> {
            WebUtilities.requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.delete(
                    WebUtilities.getUsername(req),
                    WebUtilities.getId(req));
        };


        EndpointUtilities.getForList(findForSpecificationPath, findForSpecificationRoute);
        EndpointUtilities.postForList(findBySelectorPath, findBySelectorRoute);

        EndpointUtilities.postForDatum(createPath, createRoute);
        EndpointUtilities.putForDatum(updateStatusPath, updateStatusRoute);
        EndpointUtilities.deleteForDatum(deletePath, deleteRoute);
    }
}
