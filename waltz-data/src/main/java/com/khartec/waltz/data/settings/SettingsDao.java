package com.khartec.waltz.data.settings;

import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.model.settings.SettingKind;
import com.khartec.waltz.schema.tables.records.SettingsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.schema.tables.Settings.SETTINGS;

@Repository
public class SettingsDao {

    private final DSLContext dsl;

    public static final RecordMapper<? super Record, Setting> SETTINGS_MAPPER = r -> {
        SettingsRecord record = r.into(SETTINGS);
        return ImmutableSetting.builder()
                .name(record.getName())
                .value(record.getValue())
                .kind(SettingKind.valueOf(record.getKind()))
                .build();
    };


    @Autowired
    public SettingsDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<Setting> findByKind(SettingKind kind) {
        return dsl
                .select(SETTINGS.fields())
                .from(SETTINGS)
                .where(SETTINGS.KIND.eq(kind.name()))
                .fetch(SETTINGS_MAPPER);
    }


    public Setting getByName(String name) {
        return dsl
                .select(SETTINGS.fields())
                .from(SETTINGS)
                .where(SETTINGS.NAME.eq(name))
                .fetchOne(SETTINGS_MAPPER);
    }

}
