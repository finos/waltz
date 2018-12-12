/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
