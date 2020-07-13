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

import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupEntry;
import com.khartec.waltz.service.app_group.FavouritesService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class FavouritesEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(FavouritesEndpoint.class);
    private static final String BASE_URL = mkPath("api", "favourites");

    private final FavouritesService favouritesService;


    @Autowired
    public FavouritesEndpoint(FavouritesService service) {
        this.favouritesService = service;
    }


    @Override
    public void register() {

        String getFavouriteGroupPath = mkPath(BASE_URL, "group");
        String getFavouriteGroupEntriesPath = mkPath(BASE_URL, "entries");
        String addApplicationPath = mkPath(BASE_URL, "application", ":id");
        String removeApplicationPath = mkPath(BASE_URL, "application", ":id");

        DatumRoute<AppGroup> getFavouriteGroupRoute = (request, response) ->
                favouritesService.getFavouritesGroup(getUsername(request));

        ListRoute<AppGroupEntry> getFavouriteGroupEntriesRoute = (request, response) ->
                favouritesService.getFavouriteGroupEntries(getUsername(request));

        ListRoute<AppGroupEntry> addApplicationRoute = (request, response) -> {
            long applicationId = getId(request);
            return favouritesService.addApplication(
                    getUsername(request),
                    applicationId);
        };

        ListRoute<AppGroupEntry> removeApplicationRoute = (request, response) -> {
            long applicationId = getId(request);
            return favouritesService.removeApplication(getUsername(request), applicationId);
        };

        postForList(addApplicationPath, addApplicationRoute);
        deleteForList(removeApplicationPath, removeApplicationRoute);
        getForList(getFavouriteGroupEntriesPath, getFavouriteGroupEntriesRoute);
        getForDatum(getFavouriteGroupPath, getFavouriteGroupRoute);

    }
}
