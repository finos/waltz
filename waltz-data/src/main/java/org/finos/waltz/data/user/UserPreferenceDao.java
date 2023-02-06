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

package org.finos.waltz.data.user;

import org.finos.waltz.schema.tables.records.UserPreferenceRecord;
import org.finos.waltz.model.user.ImmutableUserPreference;
import org.finos.waltz.model.user.UserPreference;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.schema.tables.UserPreference.USER_PREFERENCE;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;

@Repository
public class UserPreferenceDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceDao.class);

    private static final RecordMapper<Record, UserPreference> TO_USER_PREFERENCE_MAPPER = r -> {
        UserPreferenceRecord record = r.into(USER_PREFERENCE);
        return ImmutableUserPreference.builder()
                .key(record.getKey())
                .value(record.getValue())
                .build();
    };

    private static final BiFunction<String, UserPreference, UserPreferenceRecord> TO_RECORD_MAPPER = (userName, pref) -> {
        UserPreferenceRecord record = new UserPreferenceRecord();
        record.setUserName(userName);
        record.setKey(pref.key());
        record.setValue(pref.value());
        return record;
    };

    private final DSLContext dsl;


    @Autowired
    public UserPreferenceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<UserPreference> getPreferencesForUser(String userName) {
        return dsl.select(USER_PREFERENCE.fields())
            .from(USER_PREFERENCE)
            .where(USER_PREFERENCE.USER_NAME.eq(userName))
            .fetch(TO_USER_PREFERENCE_MAPPER);
    }


    public int savePreferencesForUser(String userName, List<UserPreference> preferences) {

        List<String> existingKeys = dsl.select()
                .from(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_NAME.eq(userName))
                .fetch(USER_PREFERENCE.KEY);

        List<UserPreferenceRecord> inserts = preferences.stream()
                .filter(p -> !existingKeys.contains(p.key()))
                .map(p -> TO_RECORD_MAPPER.apply(userName, p))
                .collect(Collectors.toList());


        List<UserPreferenceRecord> updates = preferences.stream()
                .filter(p -> existingKeys.contains(p.key()))
                .map(p -> TO_RECORD_MAPPER.apply(userName, p))
                .collect(Collectors.toList());

        int[] insertResult = dsl.batchInsert(inserts).execute();
        int[] updateResult = dsl.batchUpdate(updates).execute();

        return IntStream.of(insertResult).sum() + IntStream.of(updateResult).sum();
    }


    public int savePreference(String userName, UserPreference preference) {
        return savePreferencesForUser(userName, newArrayList(preference));
    }


    public void clearPreferencesForUser(String userName) {
        dsl.deleteFrom(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_NAME.eq(userName))
                .execute();
    }

}
