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

package org.finos.waltz.data.shared_preference;

import org.finos.waltz.schema.tables.records.SharedPreferenceRecord;
import org.finos.waltz.model.shared_preference.ImmutableSharedPreference;
import org.finos.waltz.model.shared_preference.SharedPreference;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static org.finos.waltz.schema.tables.SharedPreference.SHARED_PREFERENCE;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class SharedPreferenceDao {

    private static final Logger LOG = LoggerFactory.getLogger(SharedPreferenceDao.class);
    private final DSLContext dsl;

    private static final RecordMapper<Record, SharedPreference> TO_DOMAIN_MAPPER = r -> {
        SharedPreferenceRecord record = r.into(SHARED_PREFERENCE);
        return ImmutableSharedPreference.builder()
                .key(record.getKey())
                .category(record.getCategory())
                .value(record.getValue())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };

    public static Function<SharedPreference, SharedPreferenceRecord> TO_RECORD_MAPPER = sp -> {
        SharedPreferenceRecord spr = new SharedPreferenceRecord();
        spr.setKey(sp.key());
        spr.setCategory(sp.category());
        spr.setValue(sp.value());
        spr.setLastUpdatedAt(Timestamp.valueOf(sp.lastUpdatedAt()));
        spr.setLastUpdatedBy(sp.lastUpdatedBy());
        return spr;
    };


    @Autowired
    public SharedPreferenceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public SharedPreference getPreference(String key, String category) {
        return dsl
                .select(SHARED_PREFERENCE.fields())
                .from(SHARED_PREFERENCE)
                .where(SHARED_PREFERENCE.CATEGORY.eq(category).and(SHARED_PREFERENCE.KEY.eq(key)))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SharedPreference> findPreferencesByCategory(String category) {
        return dsl
                .select(SHARED_PREFERENCE.fields())
                .from(SHARED_PREFERENCE)
                .where(SHARED_PREFERENCE.CATEGORY.eq(category))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean savePreference(SharedPreference sharedPreference) {
        SharedPreference existing = getPreference(sharedPreference.key(), sharedPreference.category());
        SharedPreferenceRecord newRecord = TO_RECORD_MAPPER.apply(sharedPreference);

        if(existing == null) {
            LOG.debug("Inserting preference {}", sharedPreference);
            return dsl.executeInsert(newRecord) == 1;
        } else {
            LOG.debug("Updating preference {}", sharedPreference);
            return dsl.executeUpdate(newRecord) == 1;
        }
    }

}
