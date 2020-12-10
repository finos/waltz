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

package com.khartec.waltz.service.client_cache_key;


import com.khartec.waltz.data.client_cache_key.ClientCacheKeyDao;
import com.khartec.waltz.model.client_cache_key.ClientCacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.UUID.randomUUID;


@Service
public class ClientCacheKeyService {

    private final ClientCacheKeyDao clientCacheKeyDao;


    @Autowired
    public ClientCacheKeyService(ClientCacheKeyDao clientCacheKeyDao) {
        checkNotNull(clientCacheKeyDao, "clientCacheKeyDao cannot be null");
        this.clientCacheKeyDao = clientCacheKeyDao;
    }


    public List<ClientCacheKey> findAll() {
        return clientCacheKeyDao.findAll();
    }


    public ClientCacheKey getByKey(String key) {
        return clientCacheKeyDao.getByKey(key);
    }


    public ClientCacheKey createOrUpdate(String key) {
        UUID guid = randomUUID();
        clientCacheKeyDao.createOrUpdate(key, guid.toString());
        return clientCacheKeyDao.getByKey(key);
    }

}
