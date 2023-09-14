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

package org.finos.waltz.data.settings;

import org.finos.waltz.model.settings.UpdateSettingsCommand;
import org.finos.waltz.schema.tables.records.SettingsRecord;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.settings.Setting;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.schema.tables.Settings.SETTINGS;

@Repository
public class SettingsDao {

    private final DSLContext dsl;

    public static final RecordMapper<? super Record, Setting> SETTINGS_MAPPER = r -> {
        SettingsRecord record = r.into(SETTINGS);
        return ImmutableSetting
                .builder()
                .name(record.getName())
                .value(Optional.ofNullable(r.getValue(SETTINGS.VALUE)).map(String::trim))
                .restricted(record.getRestricted())
                .description(record.getDescription())
                .build();
    };


    @Autowired
    public SettingsDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<Setting> findAll() {
        return dsl
                .select(SETTINGS.fields())
                .from(SETTINGS)
                .fetch(SETTINGS_MAPPER);
    }


    public Setting getByName(String name) {
        return dsl
                .select(SETTINGS.fields())
                .from(SETTINGS)
                .where(SETTINGS.NAME.eq(name))
                .fetchOne(SETTINGS_MAPPER);
    }


    public Map<String, String> indexByPrefix(String prefix) {
        return dsl
                .select(SETTINGS.NAME, SETTINGS.VALUE)
                .from(SETTINGS)
                .where(SETTINGS.NAME.startsWith(prefix))
                .and(SETTINGS.RESTRICTED.isFalse())
                .fetchMap(
                        SETTINGS.NAME,
                        r -> StringUtilities.mkSafe(r.get(SETTINGS.VALUE)).trim());
    }


    public int update(UpdateSettingsCommand cmd) {
        return dsl
                .update(SETTINGS)
                .set(SETTINGS.VALUE, cmd.value())
                .where(SETTINGS.NAME.eq(cmd.name()))
                .and(SETTINGS.RESTRICTED.isFalse())
                .execute();

    }

    public int create(Setting setting) {
        SettingsRecord record = dsl.newRecord(SETTINGS);
        record.setName(setting.name());
        record.setDescription(setting.description());
        record.setRestricted(setting.restricted());

        setting.value().ifPresent(record::setValue);

        return record.store();
    }
}
