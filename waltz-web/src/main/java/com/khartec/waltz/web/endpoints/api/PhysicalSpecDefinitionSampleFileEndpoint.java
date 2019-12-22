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

import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFile;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionSampleFileCreateCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionSampleFileService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class PhysicalSpecDefinitionSampleFileEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-sample-file");

    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService;


    @Autowired
    public PhysicalSpecDefinitionSampleFileEndpoint(UserRoleService userRoleService,
                                                    PhysicalSpecDefinitionSampleFileService specDefinitionSampleFileService) {

        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionSampleFileService, "specDefinitionSampleFileService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionSampleFileService = specDefinitionSampleFileService;
    }

    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");
        String createPath = mkPath(BASE_URL, "spec-definition", ":id");

        DatumRoute<PhysicalSpecDefinitionSampleFile> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionSampleFileService.findForSpecDefinition(getId(req)).orElse(null);

        DatumRoute<Long> createRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            return specDefinitionSampleFileService.create(
                    getId(req),
                    readBody(req, PhysicalSpecDefinitionSampleFileCreateCommand.class));
        };

        getForDatum(findForSpecDefinitionPath, findForSpecDefinitionRoute);
        postForDatum(createPath, createRoute);

    }
}
