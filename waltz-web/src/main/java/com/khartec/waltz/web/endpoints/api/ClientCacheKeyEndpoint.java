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

import com.khartec.waltz.model.client_cache_key.ClientCacheKey;
import com.khartec.waltz.service.client_cache_key.ClientCacheKeyService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class ClientCacheKeyEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "client-cache-key");

    private final ClientCacheKeyService clientCacheKeyService;


    @Autowired
    public ClientCacheKeyEndpoint(ClientCacheKeyService clientCacheKeyService) {
        this.clientCacheKeyService = clientCacheKeyService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL, "all");
        String getByKeyPath = mkPath(BASE_URL, "key", ":key");
        String createOrUpdatePath = mkPath(BASE_URL, "update", ":key");

        ListRoute<ClientCacheKey> findAllRoute = (req, res) -> clientCacheKeyService.findAll();

        DatumRoute<ClientCacheKey> getByKeyRoute = (req, res) -> {
            String key = req.params("key");
            return clientCacheKeyService.getByKey(key);
        };

        DatumRoute<ClientCacheKey> createOrUpdateRoute = (req, res) -> {
            String key = req.params("key");
            return clientCacheKeyService.createOrUpdate(key);
        };

        getForList(findAllPath, findAllRoute);
        getForDatum(getByKeyPath, getByKeyRoute);
        postForDatum(createOrUpdatePath, createOrUpdateRoute);
    }
}
