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

import org.finos.waltz.service.involvement.InvolvementViewService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.involvement.InvolvementDetail;
import org.finos.waltz.model.involvement.InvolvementViewItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;

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
        String findKeyInvolvementsForEntityPath = mkPath(BASE_URL, "entity", "kind", ":kind", "id", ":id");
        String findInvolvementsByKindAndEntityKindPath = mkPath(BASE_URL, "involvement-kind", ":id", "entity-kind", ":kind");

        ListRoute<InvolvementViewItem> findAllByEmployeeIdRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return involvementViewService.findAllByEmployeeId(employeeId);
        };

        ListRoute<InvolvementDetail> findKeyInvolvementsForEntityRoute = (request, response) ->
                involvementViewService.findKeyInvolvementsForEntity(getEntityReference(request));


        ListRoute<InvolvementViewItem> findInvolvementsByKindAndEntityKindRoute = (request, response) ->
                involvementViewService.findByKindIdAndEntityKind(getId(request), getKind(request));

        getForList(findAllByEmployeeIdPath, findAllByEmployeeIdRoute);
        getForList(findKeyInvolvementsForEntityPath, findKeyInvolvementsForEntityRoute);
        getForList(findInvolvementsByKindAndEntityKindPath, findInvolvementsByKindAndEntityKindRoute);
    }

}
