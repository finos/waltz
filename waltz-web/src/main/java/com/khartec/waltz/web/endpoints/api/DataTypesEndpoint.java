/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

        ListRoute<DataType> searchRoute = (request, response) ->
                service.search(EntitySearchOptions
                        .mkForEntity(EntityKind.DATA_TYPE, readBody(request, String.class)));

        DatumRoute<DataType> getDataTypeByIdRoute = (request, response) ->
                service.getDataTypeById(parseLong(request.params("id")));

        getForList(BASE_URL, (request, response) -> service.findAll());
        postForList(searchPath, searchRoute);
        getForDatum(getDataTypeByIdPath, getDataTypeByIdRoute);
    }



}
