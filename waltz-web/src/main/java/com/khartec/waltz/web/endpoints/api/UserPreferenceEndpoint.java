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

import com.khartec.waltz.model.user.UserPreference;
import com.khartec.waltz.service.user.UserPreferenceService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class UserPreferenceEndpoint implements Endpoint {


    @Autowired
    public UserPreferenceEndpoint(UserPreferenceService userPreferenceService) {
        checkNotNull(userPreferenceService, "userPreferenceService cannot be null");


        this.userPreferenceService = userPreferenceService;
    }

    private static final String BASE_URL = mkPath("api", "user-preference");

    private final UserPreferenceService userPreferenceService;


    @Override
    public void register() {

        // -- paths
        String findAllForUserPath = mkPath(BASE_URL);
        String saveAllForUserPath = mkPath(BASE_URL, "save-all");
        String saveForUserPath = mkPath(BASE_URL, "save");
        String deleteForUserPath = mkPath(BASE_URL, "clear");


        // -- routes
        ListRoute<UserPreference> findAllForUserRoute = (request, response) -> {
            String userName = getUsername(request);
            return userPreferenceService.getPreferences(userName);
        };


        ListRoute<UserPreference> saveAllForUserRoute = (request, response) -> {
            String userName = getUsername(request);
            List<UserPreference> preferences = readBody(request, List.class);
            return userPreferenceService.savePreferences(userName, preferences);
        };


        ListRoute<UserPreference> saveForUserRoute = (request, response) -> {
            UserPreference preference = readBody(request, UserPreference.class);
            return userPreferenceService.savePreference(getUsername(request), preference);
        };


        DatumRoute<Boolean> deleteForUserRoute = (request, response) -> {
            String userName = getUsername(request);
            return userPreferenceService.clearPreferences(userName);
        };


        // --- register
        getForList(findAllForUserPath, findAllForUserRoute);
        postForList(saveAllForUserPath, saveAllForUserRoute);
        postForList(saveForUserPath, saveForUserRoute);
        deleteForDatum(deleteForUserPath, deleteForUserRoute);
    }
}
