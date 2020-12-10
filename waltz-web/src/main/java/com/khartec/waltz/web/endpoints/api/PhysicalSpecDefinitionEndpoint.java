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

import com.khartec.waltz.common.exception.DuplicateKeyException;
import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionChangeCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.map;
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
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);


            PhysicalSpecDefinitionChangeCommand physicalSpecDefinitionChangeCommand = readBody(req, PhysicalSpecDefinitionChangeCommand.class);

            long physicalSpecificationId = getId(req);

            Set<String> existingVersions = map(specDefinitionService.findForSpecification(physicalSpecificationId), d -> d.version());

            if (existingVersions.contains(physicalSpecDefinitionChangeCommand.version())) {
                throw new DuplicateKeyException("Cannot create physical specification definition with version that already exists");
            }

            return specDefinitionService.create(
                        getUsername(req),
                        physicalSpecificationId,
                        physicalSpecDefinitionChangeCommand);

        };

        DatumRoute<Boolean> updateStatusRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionService.updateStatus(
                    getUsername(req),
                    getId(req),
                    readBody(req, ReleaseLifecycleStatusChangeCommand.class));
        };

        DatumRoute<Integer> deleteRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

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
