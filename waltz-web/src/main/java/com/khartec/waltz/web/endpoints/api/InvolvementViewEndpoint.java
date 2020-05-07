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

import com.khartec.waltz.model.involvement.InvolvementViewItem;
import com.khartec.waltz.service.involvement.InvolvementViewService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class InvolvementViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "involvement-view");

    private final InvolvementViewService involvementViewService;


    @Autowired
    public InvolvementViewEndpoint(InvolvementViewService involvementViewService) {
        this.involvementViewService = involvementViewService;
    }


    @Override
    public void register() {

        String findAllByEmployeeIdPath = mkPath(BASE_URL, "employee", ":employeeId");

        ListRoute<InvolvementViewItem> findAllByEmployeeIdRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return involvementViewService.findAllByEmployeeId(employeeId);
        };

        getForList(findAllByEmployeeIdPath, findAllByEmployeeIdRoute);
    }

}
