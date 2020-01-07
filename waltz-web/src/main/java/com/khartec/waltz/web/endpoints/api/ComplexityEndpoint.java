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

import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class ComplexityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "complexity");

    private final ComplexityRatingService service;


    @Autowired
    public ComplexityEndpoint(ComplexityRatingService service) {
        this.service = service;
    }


    @Override
    public void register() {
        String getForAppPath = mkPath(BASE_URL, "application", ":id");
        String findForAppIdSelectorPath = BASE_URL;
        String rebuildPath = mkPath(BASE_URL, "rebuild");

        DatumRoute<ComplexityRating> getForAppRoute = (request, response) -> service.getForApp(getId(request));
        ListRoute<ComplexityRating> findForAppIdSelectorRoute = (request, response) -> service.findForAppIdSelector(readIdSelectionOptionsFromBody(request));
        DatumRoute<Integer> rebuildRoute = (request, response) -> service.rebuild();

        getForDatum(getForAppPath, getForAppRoute);
        postForList(findForAppIdSelectorPath, findForAppIdSelectorRoute);
        getForDatum(rebuildPath, rebuildRoute);
    }
}
