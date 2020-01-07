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


import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.end_user_app.EndUserAppService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

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

        getForList(countByOrgUnitPath, countByOrgUnitRoute);

        postForList(findBySelectorPath, findBySelectorRoute);

        postForDatum(promoteToApplicationPath, promoteToApplicationRoute);

        getForList(findAllPath, findAllRoute);

    }

}
