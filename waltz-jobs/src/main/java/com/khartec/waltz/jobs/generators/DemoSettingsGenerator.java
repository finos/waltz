/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
