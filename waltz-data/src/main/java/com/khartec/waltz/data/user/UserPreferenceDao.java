/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

package com.khartec.waltz.data.user;

import com.khartec.waltz.model.user.ImmutableUserPreference;
import com.khartec.waltz.model.user.UserPreference;
import com.khartec.waltz.schema.tables.records.UserPreferenceRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.UserPreference.USER_PREFERENCE;

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
