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

import org.finos.waltz.service.complexity_kind.ComplexityKindService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.complexity.ComplexityKind;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ComplexityKindEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "complexity-kind");

    private final ComplexityKindService complexityKindService;


    @Autowired
    public ComplexityKindEndpoint(ComplexityKindService complexityKindService) {
        this.complexityKindService = complexityKindService;
    }


    @Override

    public void register() {

        ListRoute<ComplexityKind> findAllRoute = (request, response) -> complexityKindService.findAll();
        ListRoute<ComplexityKind> findBySelectorRoute = (request, response) -> complexityKindService.findBySelector(WebUtilities.getKind(request), WebUtilities.readIdSelectionOptionsFromBody(request));

        EndpointUtilities.getForList(BASE_URL, findAllRoute);
        EndpointUtilities.postForList(WebUtilities.mkPath(BASE_URL, "target-kind", ":kind", "selector"), findBySelectorRoute);
    }
}
