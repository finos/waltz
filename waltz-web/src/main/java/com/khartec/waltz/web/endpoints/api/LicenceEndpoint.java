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

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.licence.Licence;
import com.khartec.waltz.service.licence.LicenceService;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class LicenceEndpoint implements Endpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LicenceEndpoint.class);
    private static final String BASE_URL = mkPath("api", "licence");

    private final LicenceService service;


    @Autowired
    public LicenceEndpoint(LicenceService service) {
        checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {

        // read
        getForList(mkPath(BASE_URL, "all"), (request, response) -> service.findAll());
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute );
        getForDatum(mkPath(BASE_URL, "external-id", ":externalId"), this::getByExternalIdRoute );
        getForList(mkPath(BASE_URL, "count", "application"), (request, response) -> service.countApplications());
        postForList(mkPath(BASE_URL, "selector"), this::findBySelectorRoute);
    }


    private Licence getByExternalIdRoute(Request request, Response response) {
        String externalId = request.params("externalId");
        return service.getByExternalId(externalId);
    }


    private Licence getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private  List<Licence> findBySelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);
        return service.findBySelector(options);
    }
}
