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


import com.khartec.waltz.model.attribute_change.AttributeChange;
import com.khartec.waltz.service.attribute_change.AttributeChangeService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static java.lang.Long.parseLong;


@Service
public class AttributeChangeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "attribute-change");

    private final AttributeChangeService service;


    @Autowired
    public AttributeChangeEndpoint(AttributeChangeService service) {
        checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByChangeUnitIdPath = mkPath(BASE_URL, "change-unit", ":id");

        DatumRoute<AttributeChange> getByIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.getById(parseLong(id));
        };

        ListRoute<AttributeChange> findByChangeUnitIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.findByChangeUnitId(parseLong(id));
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByChangeUnitIdPath, findByChangeUnitIdRoute);
    }


}
