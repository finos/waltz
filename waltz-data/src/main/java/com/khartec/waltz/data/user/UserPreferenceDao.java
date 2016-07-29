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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.schema.tables.UserPreference.USER_PREFERENCE;

@Repository
public class UserPreferenceDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceDao.class);

    private static final RecordMapper<Record, UserPreference> TO_USER_PREFERENCE_MAPPER = r -> {

        UserPreferenceRecord record = r.into(USER_PREFERENCE);
        return ImmutableUserPreference.builder()
                .user_id(record.getUserId())
                .key(record.getKey())
                .value(record.getValue())
                .build();
    };

    private static final Function<UserPreference, UserPreferenceRecord> TO_RECORD_MAPPER = p -> {
        UserPreferenceRecord record = new UserPreferenceRecord();
        record.setUserId(p.user_id());
        record.setKey(p.key());
        record.setValue(p.value());
        return record;
    };

    private final DSLContext dsl;


    @Autowired
    public UserPreferenceDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<UserPreference> getPreferencesForUser(String userId) {
        return dsl.select(USER_PREFERENCE.fields())
            .from(USER_PREFERENCE)
            .where(USER_PREFERENCE.USER_ID.eq(userId))
            .fetch(TO_USER_PREFERENCE_MAPPER);
    }


    public int savePreferencesForUser(String userId, List<UserPreference> preferences) {

        List<String> existingKeys = dsl.select(USER_PREFERENCE.KEY)
                .from(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_ID.eq(userId))
                .fetch()
                .stream()
                .map(up -> up.getValue(USER_PREFERENCE.KEY))
                .collect(Collectors.toList());

        List<UserPreferenceRecord> inserts = preferences.stream()
                .filter(p -> p.user_id().equals(userId) && !existingKeys.contains(p.key()))
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toList());


        List<UserPreferenceRecord> updates = preferences.stream()
                .filter(p -> p.user_id().equals(userId) && existingKeys.contains(p.key()))
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toList());

        int[] insertResult = dsl.batchInsert(inserts).execute();
        int[] updateResult = dsl.batchUpdate(updates).execute();

        return IntStream.of(insertResult).sum() + IntStream.of(updateResult).sum();
    }


    public void clearPreferencesForUser(String userId) {

        dsl.deleteFrom(USER_PREFERENCE)
                .where(USER_PREFERENCE.USER_ID.eq(userId))
                .execute();
    }


}
