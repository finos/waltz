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

import org.finos.waltz.service.shared_preference.SharedPreferenceService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.json.SharedPreferenceKeyAndCategory;
import org.finos.waltz.model.shared_preference.SharedPreference;
import org.finos.waltz.model.shared_preference.SharedPreferenceSaveCommand;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DigestUtilities.digest;


@Service
public class SharedPreferenceEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "shared-preference");

    private final SharedPreferenceService sharedPreferenceService;


    @Autowired
    public SharedPreferenceEndpoint(SharedPreferenceService sharedPreferenceService) {
        checkNotNull(sharedPreferenceService, "sharedPreferenceService cannot be null");
        this.sharedPreferenceService = sharedPreferenceService;
    }


    @Override
    public void register() {
        String getByKeyAndCategoryPath = WebUtilities.mkPath(BASE_URL, "key-category");
        String findByCategoryPath = WebUtilities.mkPath(BASE_URL, "category", ":category");
        String savePath = WebUtilities.mkPath(BASE_URL, "save");
        String generateKeyPath = WebUtilities.mkPath(BASE_URL, "generate-key");

        EndpointUtilities.postForDatum(getByKeyAndCategoryPath, this::getByKeyAndCategoryRoute);
        EndpointUtilities.getForList(findByCategoryPath, this::findByCategoryRoute);
        EndpointUtilities.postForDatum(generateKeyPath, this::generateKeyRoute);
        EndpointUtilities.postForDatum(savePath, this::saveRoute);
    }


    private SharedPreference getByKeyAndCategoryRoute(Request request, Response response) throws IOException {
        SharedPreferenceKeyAndCategory keyCat = WebUtilities.readBody(request, SharedPreferenceKeyAndCategory.class);
        return sharedPreferenceService.getPreference(keyCat.key(), keyCat.category());
    }


    private List<SharedPreference> findByCategoryRoute(Request request, Response response) {
        String category = request.params("category");
        return sharedPreferenceService.findPreferencesByCategory(category);
    }


    private String generateKeyRoute(Request request, Response response) throws NoSuchAlgorithmException {
        return digest(request.body().getBytes());
    }


    private boolean saveRoute(Request request, Response response) throws IOException {
        String username = WebUtilities.getUsername(request);
        SharedPreferenceSaveCommand sp = WebUtilities.readBody(request, SharedPreferenceSaveCommand.class);
        return sharedPreferenceService.savePreference(username, sp);
    }

}
