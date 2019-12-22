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
import com.khartec.waltz.model.logical_data_element.LogicalDataElementChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionFieldService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.util.stream.Collectors.toList;

@Service
public class PhysicalSpecDefinitionFieldEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-definition-field");


    private final UserRoleService userRoleService;

    private final PhysicalSpecDefinitionFieldService specDefinitionFieldService;


    @Autowired
    public PhysicalSpecDefinitionFieldEndpoint(UserRoleService userRoleService,
                                               PhysicalSpecDefinitionFieldService specDefinitionFieldService) {
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(specDefinitionFieldService, "specDefinitionFieldService cannot be null");

        this.userRoleService = userRoleService;
        this.specDefinitionFieldService = specDefinitionFieldService;
    }


    @Override
    public void register() {
        String findForSpecDefinitionPath = mkPath(BASE_URL, "spec-definition", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String createFieldsPath = mkPath(BASE_URL, "spec-definition", ":id", "fields");
        String updateDescriptionPath = mkPath(BASE_URL, ":id", "description");
        String updateLogicalElementPath = mkPath(BASE_URL, ":id", "logical-data-element");

        ListRoute<PhysicalSpecDefinitionField> findForSpecDefinitionRoute =
                (req, res) -> specDefinitionFieldService.findForSpecDefinition(getId(req));

        ListRoute<PhysicalSpecDefinitionField> findBySelectorRoute =
                (req, res) -> specDefinitionFieldService.findBySelector(readIdSelectionOptionsFromBody(req));

        ListRoute<Long> createFieldsRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            String userName = getUsername(req);
            long specDefinitionId = getId(req);
            PhysicalSpecDefinitionFieldChangeCommand[] commands = readBody(
                    req,
                    PhysicalSpecDefinitionFieldChangeCommand[].class);

            return Arrays.stream(commands)
                    .map(c -> specDefinitionFieldService.create(userName, specDefinitionId, c))
                    .collect(toList());
        };

        DatumRoute<Integer> updateDescriptionRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            res.type(WebUtilities.TYPE_JSON);
            UpdateDescriptionCommand command = readBody(req, UpdateDescriptionCommand.class);

            return specDefinitionFieldService.updateDescription(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command);
        };

        DatumRoute<Integer> updateLogicalElementRoute = (req, res) -> {
            requireRole(userRoleService, req, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

            res.type(WebUtilities.TYPE_JSON);
            LogicalDataElementChangeCommand command = readBody(req, LogicalDataElementChangeCommand.class);

            return specDefinitionFieldService.updateLogicalDataElement(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command);
        };

        getForList(findForSpecDefinitionPath, findForSpecDefinitionRoute);
        postForList(findBySelectorPath, findBySelectorRoute);

        putForDatum(updateDescriptionPath, updateDescriptionRoute);
        putForDatum(updateLogicalElementPath, updateLogicalElementRoute);

        postForList(createFieldsPath, createFieldsRoute);
    }
}
