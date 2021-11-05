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

package com.khartec.waltz.service.database_usage;

import org.finos.waltz.data.database_usage.DatabaseUsageDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.database_usage.DatabaseUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DatabaseUsageService {

    private final DatabaseUsageDao databaseUsageDao;


    @Autowired
    public DatabaseUsageService(DatabaseUsageDao databaseUsageDao) {
        this.databaseUsageDao = databaseUsageDao;
    }


    public Collection<DatabaseUsage> findByDatabaseId(long databaseId) {
        return databaseUsageDao.findByDatabaseId(databaseId);
    }


    public Collection<DatabaseUsage> findByEntityReference(EntityReference ref) {
        return databaseUsageDao.findByEntityReference(ref);
    }
}
