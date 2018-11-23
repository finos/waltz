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
