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

package com.khartec.waltz.data.settings;

import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
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
                .restricted(record.getRestricted())
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

}
