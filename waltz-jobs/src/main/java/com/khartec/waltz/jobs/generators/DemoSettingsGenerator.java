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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.schema.tables.records.SettingsRecord;
import com.khartec.waltz.service.settings.SettingsService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.SETTINGS;

public class DemoSettingsGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<SettingsRecord> records = newArrayList(
                mkSetting(SettingsService.DEFAULT_ROLES_KEY, SystemRole.BOOKMARK_EDITOR.name()),
                mkSetting("web.authentication", "waltz"),
                mkSetting("ui.avatar.template.url", "https://gravatar.com/avatar/${id}?s=200&d=robohash&r=pg"),
                mkSetting("ui.logo.overlay.text", "Demo"),
                mkSetting("ui.logo.overlay.color", "#9c9"));

        dsl.batchInsert(records).execute();

        return null;
    }

    private SettingsRecord mkSetting(String defaultRolesKey, String name) {
        return new SettingsRecord(defaultRolesKey, name, false);
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx).deleteFrom(SETTINGS).execute();
        return false;
    }
}
