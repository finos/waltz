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


import com.khartec.waltz.model.flow_classification.FlowClassification;
import com.khartec.waltz.service.flow_classification_rule.FlowClassificationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class FlowClassificationEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(FlowClassificationEndpoint.class);
    private static final String BASE_URL = mkPath("api", "flow-classification");

    private final FlowClassificationService flowClassificationService;
    private final UserRoleService userRoleService;


    @Autowired
    public FlowClassificationEndpoint(FlowClassificationService flowClassificationService,
                                      UserRoleService userRoleService) {
        checkNotNull(flowClassificationService, "flowClassificationService must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.flowClassificationService = flowClassificationService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // -- PATHS

        String getByIdPath = mkPath(BASE_URL, "id", ":id");


        // -- ROUTES

        ListRoute<FlowClassification> findAllRoute = (request, response)
                -> flowClassificationService.findAll();

        DatumRoute<FlowClassification> getByIdRoute = (request, response)
                -> flowClassificationService.getById(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
        getForList(BASE_URL, findAllRoute);
    }
}
