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
import com.khartec.waltz.model.logical_data_element.LogicalDataElementChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import com.khartec.waltz.model.user.Role;
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
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

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
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

            res.type(WebUtilities.TYPE_JSON);
            UpdateDescriptionCommand command = readBody(req, UpdateDescriptionCommand.class);

            return specDefinitionFieldService.updateDescription(
                    WebUtilities.getUsername(req),
                    getId(req),
                    command);
        };

        DatumRoute<Integer> updateLogicalElementRoute = (req, res) -> {
            requireRole(userRoleService, req, Role.LOGICAL_DATA_FLOW_EDITOR);

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
