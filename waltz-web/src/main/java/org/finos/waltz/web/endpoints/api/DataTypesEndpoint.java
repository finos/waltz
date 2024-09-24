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

import org.finos.waltz.model.datatype.DataTypeMigrationResult;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static java.lang.Long.parseLong;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class DataTypesEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "data-types");

    private final DataTypeService dataTypeService;
    private final UserRoleService userRoleService;

    @Autowired
    public DataTypesEndpoint(DataTypeService dataTypeService, UserRoleService userRoleService) {
        checkNotNull(dataTypeService, "dataTypeService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");
        this.userRoleService = userRoleService;
        this.dataTypeService = dataTypeService;
    }


    @Override
    public void register() {
        String searchPath = mkPath(BASE_URL, "search");
        String getDataTypeByIdPath = mkPath(BASE_URL, "id", ":id");
        String getDataTypeByCodePath = mkPath(BASE_URL, "code", ":code");
        String findSuggestedByEntityRefPath = mkPath(BASE_URL, "suggested", "entity", ":kind", ":id");
        String findByParentIdPath = mkPath(BASE_URL, "parent-id", ":id");
        String migratePath = mkPath(BASE_URL, "migrate", ":sourceDataTypeId", "to", ":targetDataTypeId");

        ListRoute<DataType> searchRoute = (request, response) ->
                dataTypeService.search(EntitySearchOptions
                        .mkForEntity(EntityKind.DATA_TYPE, readBody(request, String.class)));

        DatumRoute<DataType> getDataTypeByIdRoute = (request, response) ->
                dataTypeService.getDataTypeById(parseLong(request.params("id")));

        DatumRoute<DataType> getDataTypeByCodeRoute = (request, response) ->
                dataTypeService.getDataTypeByCode(request.params("code"));

        ListRoute<DataType> findSuggestedByEntityRefRoute = (req, res) ->
                dataTypeService.findSuggestedByEntityRef(getEntityReference(req));

        ListRoute<DataType> findByParentIdRoute = (req, res) ->
                dataTypeService.findByParentId(getId(req));

        DatumRoute<DataTypeMigrationResult> migrateRoute = (request, response) -> {
            ensureUserHasMigrateAdminRights(request);
            long sourceDataTypeId = getLong(request, "sourceDataTypeId");
            long targetDataTypeId = getLong(request, "targetDataTypeId");
            boolean removeSource = Boolean.parseBoolean(request.queryParams("removeSource"));
            return dataTypeService.migrate(sourceDataTypeId, targetDataTypeId, removeSource);
        };

        getForList(BASE_URL, (request, response) -> dataTypeService.findAll());
        postForList(searchPath, searchRoute);
        getForDatum(getDataTypeByIdPath, getDataTypeByIdRoute);
        getForDatum(getDataTypeByCodePath, getDataTypeByCodeRoute);
        getForList(findSuggestedByEntityRefPath, findSuggestedByEntityRefRoute);
        getForList(findByParentIdPath, findByParentIdRoute);
        postForDatum(migratePath, migrateRoute);
    }

    // -- HELPERS ---

    private void ensureUserHasMigrateAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }



}
