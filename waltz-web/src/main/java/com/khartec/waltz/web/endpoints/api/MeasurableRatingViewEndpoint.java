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

import com.khartec.waltz.model.measurable_rating.MeasurableRatingGridView;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingViewService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class MeasurableRatingViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-rating-view");

    private final MeasurableRatingViewService measurableRatingViewService;


    @Autowired
    public MeasurableRatingViewEndpoint(MeasurableRatingViewService measurableRatingViewService) {
        this.measurableRatingViewService = measurableRatingViewService;
    }


    @Override
    public void register() {
        String findByCategoryIdPath = mkPath(BASE_URL, "category-id", ":categoryId");
        String findByCategoryExtIdPath = mkPath(BASE_URL, "category-external-id", ":categoryExtId");

        postForDatum(findByCategoryIdPath, this::findByCategoryIdRoute);
        postForDatum(findByCategoryExtIdPath, this::findByCategoryExtIdRoute);
    }


    public MeasurableRatingGridView findByCategoryIdRoute(Request req, Response resp) throws IOException {
        return measurableRatingViewService.findByCategoryIdAndSelectionOptions(
                getLong(req, "categoryId"),
                readIdSelectionOptionsFromBody(req));
    }


    public MeasurableRatingGridView findByCategoryExtIdRoute(Request req, Response resp) throws IOException {
        return measurableRatingViewService.findByCategoryExtIdAndSelectionOptions(
                req.params("categoryExtId"),
                readIdSelectionOptionsFromBody(req));
    }
}
