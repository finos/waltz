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


import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.action.UpdateDataTypeDecoratorAction;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.getEntityReference;
import static org.finos.waltz.web.WebUtilities.getKind;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static org.finos.waltz.web.WebUtilities.readIdsFromBody;
import static org.finos.waltz.web.WebUtilities.requireRole;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class DataTypeDecoratorEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-type-decorator");

    private final DataTypeDecoratorService dataTypeDecoratorService;
    private final UserRoleService userRoleService;
    private final DataTypeService dataTypeService;


    @Autowired
    public DataTypeDecoratorEndpoint(DataTypeDecoratorService dataTypeDecoratorService,
                                     DataTypeService dataTypeService,
                                     UserRoleService userRoleService) {
        checkNotNull(dataTypeDecoratorService, "DataTypeDecoratorService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");

        this.dataTypeDecoratorService = dataTypeDecoratorService;
        this.userRoleService = userRoleService;
        this.dataTypeService = dataTypeService;
    }


    @Override
    public void register() {
        String findByEntityReference = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector", "targetKind", ":targetKind");
        String findSuggestedByEntityRefPath = mkPath(BASE_URL, "suggested", "entity", ":kind", ":id");

        String findByFlowIdsAndKindPath = mkPath(BASE_URL, "flow-ids", "kind", ":kind");
        String updateDataTypesPath = mkPath(BASE_URL, "save", "entity", ":kind", ":id");
        String findDatatypeUsageCharacteristicsPath = mkPath(BASE_URL, "entity", ":kind", ":id", "usage-characteristics");

        ListRoute<DataTypeDecorator> findByEntityReferenceRoute = (req, res) ->
                dataTypeDecoratorService.findByEntityId(getEntityReference(req));

        ListRoute<DataTypeDecorator> findBySelectorRoute = (req, res) ->
                dataTypeDecoratorService.findByEntityIdSelector(
                    getKind(req, "targetKind"),
                    readIdSelectionOptionsFromBody(req));

        ListRoute<DataTypeDecorator> findByFlowIdsAndKindRoute = (request, response) ->
                dataTypeDecoratorService
                    .findByFlowIds(
                        readIdsFromBody(request),
                        getKind(request));

        ListRoute<DataType> findSuggestedByEntityRefRoute = (req, res) ->
                dataTypeService.findSuggestedByEntityRef(getEntityReference(req));

        ListRoute<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristicsRoute = (req, res) ->
                dataTypeDecoratorService
                    .findDatatypeUsageCharacteristics(getEntityReference(req));

        getForList(findByEntityReference, findByEntityReferenceRoute);
        getForList(findSuggestedByEntityRefPath, findSuggestedByEntityRefRoute);
        getForList(findDatatypeUsageCharacteristicsPath, findDatatypeUsageCharacteristicsRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForList(findByFlowIdsAndKindPath, findByFlowIdsAndKindRoute);
        postForDatum(updateDataTypesPath, this::updateDataTypesRoute);
    }


    private boolean updateDataTypesRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        String userName = WebUtilities.getUsername(request);
        UpdateDataTypeDecoratorAction action = readBody(request, UpdateDataTypeDecoratorAction.class);

        return dataTypeDecoratorService.updateDecorators(userName,
                action.entityReference(),
                action.addedDataTypeIds(),
                action.removedDataTypeIds());
    }
}
