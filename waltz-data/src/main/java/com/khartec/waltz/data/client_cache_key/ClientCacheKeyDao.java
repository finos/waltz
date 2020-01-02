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

package com.khartec.waltz.data.client_cache_key;

import com.khartec.waltz.model.client_cache_key.ClientCacheKey;
import com.khartec.waltz.model.client_cache_key.ImmutableClientCacheKey;
import com.khartec.waltz.schema.tables.records.ClientCacheKeyRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.schema.tables.ClientCacheKey.CLIENT_CACHE_KEY;


@Repository
public class ClientCacheKeyDao {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCacheKeyDao.class);

    public static final RecordMapper<Record, ClientCacheKey> TO_DOMAIN_MAPPER = (r) -> {
        ClientCacheKeyRecord record = r.into(CLIENT_CACHE_KEY);
        return ImmutableClientCacheKey.builder()
                .key(record.getKey())
                .guid(record.getGuid())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .build();
    };

    private final DSLContext dsl;


    public ClientCacheKeyDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<ClientCacheKey> findAll() {
        return dsl
                .selectFrom(CLIENT_CACHE_KEY)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public ClientCacheKey getByKey(String key) {
        return dsl
                .selectFrom(CLIENT_CACHE_KEY)
                .where(CLIENT_CACHE_KEY.KEY.eq(key))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public boolean createOrUpdate(String key, String guid) {
        return dsl.insertInto(
                    CLIENT_CACHE_KEY,
                    CLIENT_CACHE_KEY.KEY,
                    CLIENT_CACHE_KEY.GUID,
                    CLIENT_CACHE_KEY.LAST_UPDATED_AT)
                .values(key, guid, nowUtcTimestamp())
                .onDuplicateKeyUpdate()
                .set(CLIENT_CACHE_KEY.GUID, guid)
                .set(CLIENT_CACHE_KEY.LAST_UPDATED_AT, nowUtcTimestamp())
                .execute() == 1;
    }

}
