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

package com.khartec.waltz.service.server_usage;

import com.khartec.waltz.data.server_usage.ServerUsageDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.server_usage.ServerUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ServerUsageService {

    private final ServerUsageDao serverUsageDao;


    @Autowired
    public ServerUsageService(ServerUsageDao serverUsageDao) {
        checkNotNull(serverUsageDao, "serverUsageDao cannot be null");
        this.serverUsageDao = serverUsageDao;
    }


    public Collection<ServerUsage> findByServerId(long serverId) {
        return serverUsageDao.findByServerId(serverId);
    }


    public Collection<ServerUsage> findByReferencedEntity(EntityReference ref) {
        return serverUsageDao.findByReferencedEntity(ref);
    }
}
