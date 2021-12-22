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


import org.finos.waltz.service.end_user_app.EndUserAppService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.application.AppRegistrationResponse;
import org.finos.waltz.model.enduserapp.EndUserApplication;
import org.finos.waltz.model.tally.Tally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class EndUserAppEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "end-user-application");

    private final EndUserAppService endUserAppService;

    @Autowired
    public EndUserAppEndpoint(EndUserAppService endUserAppService) {
        this.endUserAppService = endUserAppService;
    }


    @Override
    public void register() {
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String countByOrgUnitPath = mkPath(BASE_URL, "count-by", "org-unit");
        String promoteToApplicationPath = mkPath(BASE_URL, "promote", ":id");

        String findAllPath = mkPath(BASE_URL);
        ListRoute<EndUserApplication> findAllRoute = (request, response)
                -> endUserAppService.findAll();

        ListRoute<EndUserApplication> findBySelectorRoute = (request, response)
                -> endUserAppService.findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<Tally<Long>> countByOrgUnitRoute = (request, response) -> endUserAppService.countByOrgUnitId();


        DatumRoute<AppRegistrationResponse> promoteToApplicationRoute = (req, res) -> {

            res.type(WebUtilities.TYPE_JSON);

            return endUserAppService.promoteToApplication(getId(req), getUsername(req));
        };

        DatumRoute<EndUserApplication> getByIdRoute = (req, res) -> endUserAppService.getById(getId(req));

        getForList(countByOrgUnitPath, countByOrgUnitRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(promoteToApplicationPath, promoteToApplicationRoute);
        getForList(findAllPath, findAllRoute);

    }

}
