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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.UserPreference.USER_PREFERENCE;

@Repository
public class UserPreferenceDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceDao.class);

    private static final RecordMapper<Record, UserPreference> TO_USER_PREFERENCE_MAPPER = r -> {

        UserPreferenceRecord record = r.into(USER_PREFERENCE);
        return ImmutableUserPreference.builder()
                .userName(record.getUserName())
                .key(record.getKey())
                .value(record.getValue())
                .build();
    };

    private static final Function<UserPreference, UserPreferenceRecord> TO_RECORD_MAPPER = p -> {
        UserPreferenceRecord record = new UserPreferenceRecord();
        record.setUserName(p.userName());
        record.setKey(p.key());
        record.setValue(p.value());
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

        List<String> existingKeys = dsl.select(USER_PREFERENCE.KEY)
                .from(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_NAME.eq(userName))
                .fetch()
                .stream()
                .map(up -> up.getValue(USER_PREFERENCE.KEY))
                .collect(Collectors.toList());

        List<UserPreferenceRecord> inserts = preferences.stream()
                .filter(p -> p.userName().equals(userName) && !existingKeys.contains(p.key()))
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toList());


        List<UserPreferenceRecord> updates = preferences.stream()
                .filter(p -> p.userName().equals(userName) && existingKeys.contains(p.key()))
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toList());

        int[] insertResult = dsl.batchInsert(inserts).execute();
        int[] updateResult = dsl.batchUpdate(updates).execute();

        return IntStream.of(insertResult).sum() + IntStream.of(updateResult).sum();
    }


    public int savePreference(UserPreference preference) {
        return savePreferencesForUser(preference.userName(),
                new ArrayList<UserPreference>() {{ add(preference);}});
    }


    public void clearPreferencesForUser(String userName) {

        dsl.deleteFrom(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_NAME.eq(userName))
                .execute();
    }


}
