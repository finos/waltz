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


import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.action.UpdatePhysicalSpecDataTypesAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.notEmpty;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class PhysicalSpecDataTypeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-spec-data-type");

    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;
    private final UserRoleService userRoleService;


    @Autowired
    public PhysicalSpecDataTypeEndpoint(PhysicalSpecDataTypeService physicalSpecDataTypeService,
                                        UserRoleService userRoleService) {
        checkNotNull(physicalSpecDataTypeService, "physicalSpecDataTypeService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findBySpecificationPath = mkPath(BASE_URL, "specification", ":id");
        String findBySpecificationSelectorPath = mkPath(BASE_URL, "specification", "selector");
        String updateDataTypesPath = mkPath(BASE_URL, "specification", ":id");

        ListRoute<PhysicalSpecificationDataType> findBySpecificationRoute = (req, res)
                -> physicalSpecDataTypeService.findBySpecificationId(getId(req));

        ListRoute<PhysicalSpecificationDataType> findBySpecificationSelectorRoute = (req, res)
                -> physicalSpecDataTypeService.findBySpecificationIdSelector(readIdSelectionOptionsFromBody(req));

        getForList(findBySpecificationPath, findBySpecificationRoute);
        postForList(findBySpecificationSelectorPath, findBySpecificationSelectorRoute);
        postForDatum(updateDataTypesPath, this::updateDataTypesRoute);
    }


    private boolean updateDataTypesRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        String userName = getUsername(request);
        UpdatePhysicalSpecDataTypesAction action = readBody(request, UpdatePhysicalSpecDataTypesAction.class);

        if (notEmpty(action.addedDataTypeIds())) {
            physicalSpecDataTypeService.addDataTypes(userName, action.specificationId(), action.addedDataTypeIds());
        }
        if (notEmpty(action.removedDataTypeIds())) {
            physicalSpecDataTypeService.removeDataTypes(userName, action.specificationId(), action.removedDataTypeIds());
        }

        return true;
    }
}
