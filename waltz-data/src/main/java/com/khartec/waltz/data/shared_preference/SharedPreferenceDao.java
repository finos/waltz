package com.khartec.waltz.data.shared_preference;

import com.khartec.waltz.model.shared_preference.ImmutableSharedPreference;
import com.khartec.waltz.model.shared_preference.SharedPreference;
import com.khartec.waltz.schema.tables.records.SharedPreferenceRecord;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SharedPreference.SHARED_PREFERENCE;

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
        return dsl.selectFrom(SHARED_PREFERENCE)
                .where(SHARED_PREFERENCE.CATEGORY.eq(category).and(SHARED_PREFERENCE.KEY.eq(key)))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SharedPreference> findPreferencesByCategory(String category) {
        return dsl.selectFrom(SHARED_PREFERENCE)
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
