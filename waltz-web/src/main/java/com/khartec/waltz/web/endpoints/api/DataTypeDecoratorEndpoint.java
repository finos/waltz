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


import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.action.UpdateDataTypeDecoratorAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class DataTypeDecoratorEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-type-decorator");

    private final DataTypeDecoratorService dataTypeDecoratorService;
    private final UserRoleService userRoleService;


    @Autowired
    public DataTypeDecoratorEndpoint(DataTypeDecoratorService dataTypeDecoratorService,
                                     UserRoleService userRoleService) {
        checkNotNull(dataTypeDecoratorService, "DataTypeDecoratorService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.dataTypeDecoratorService = dataTypeDecoratorService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityReference = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector", "targetKind", ":targetKind");

        String findByFlowIdsAndKindPath = mkPath(BASE_URL, "flow-ids", "kind", ":kind");
        String updateDataTypesPath = mkPath(BASE_URL, "save", "entity", ":kind", ":id");

        ListRoute<DataTypeDecorator> findByEntityReferenceRoute = (req, res)
                -> dataTypeDecoratorService.findByEntityId(getEntityReference(req));

        ListRoute<DataTypeDecorator> findBySelectorRoute = (req, res)
                -> dataTypeDecoratorService.findByEntityIdSelector(
                        getKind(req, "targetKind"),
                readIdSelectionOptionsFromBody(req));

        ListRoute<DataTypeDecorator> findByFlowIdsAndKindRoute =
                (request, response) -> dataTypeDecoratorService
                        .findByFlowIds(
                                readIdsFromBody(request), getKind(request));

        getForList(findByEntityReference, findByEntityReferenceRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForList(findByFlowIdsAndKindPath, findByFlowIdsAndKindRoute);
        postForDatum(updateDataTypesPath, this::updateDataTypesRoute);
    }


    private boolean updateDataTypesRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        String userName = getUsername(request);
        UpdateDataTypeDecoratorAction action = readBody(request, UpdateDataTypeDecoratorAction.class);

        return dataTypeDecoratorService.updateDecorators(userName,
                action.entityReference(),
                action.addedDataTypeIds(),
                action.removedDataTypeIds());
    }
}
