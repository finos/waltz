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

import org.finos.waltz.service.source_data_rating.SourceDataRatingService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.common.Checks;
import org.finos.waltz.model.source_data_rating.SourceDataRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class SourceDataRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "source-data-rating");

    private final SourceDataRatingService service;

    @Autowired
    public SourceDataRatingEndpoint(SourceDataRatingService service) {
        Checks.checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);

        ListRoute<SourceDataRating> findAllRoute = (request, response) ->
                service.findAll();

        getForList(findAllPath, findAllRoute);
    }
}
