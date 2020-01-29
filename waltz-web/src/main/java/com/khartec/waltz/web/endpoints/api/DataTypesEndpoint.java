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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.lang.Long.parseLong;


@Service
public class DataTypesEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "data-types");

    private final DataTypeService service;

    @Autowired
    public DataTypesEndpoint(DataTypeService service) {
        checkNotNull(service, "service must not be null");
        this.service = service;
    }


    @Override
    public void register() {
        String searchPath = mkPath(BASE_URL, "search");
        String getDataTypeByIdPath = mkPath(BASE_URL, "id", ":id");
        String getDataTypeByCodePath = mkPath(BASE_URL, "code", ":code");

        ListRoute<DataType> searchRoute = (request, response) ->
                service.search(EntitySearchOptions
                        .mkForEntity(EntityKind.DATA_TYPE, readBody(request, String.class)));

        DatumRoute<DataType> getDataTypeByIdRoute = (request, response) ->
                service.getDataTypeById(parseLong(request.params("id")));

        DatumRoute<DataType> getDataTypeByCodeRoute = (request, response) ->
                service.getDataTypeByCode(request.params("code"));

        getForList(BASE_URL, (request, response) -> service.findAll());
        postForList(searchPath, searchRoute);
        getForDatum(getDataTypeByIdPath, getDataTypeByIdRoute);
        getForDatum(getDataTypeByCodePath, getDataTypeByCodeRoute);
    }



}
