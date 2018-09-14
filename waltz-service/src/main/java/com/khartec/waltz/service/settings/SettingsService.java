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

package com.khartec.waltz.service.settings;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.settings.SettingsDao;
import com.khartec.waltz.model.settings.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.ListUtilities.ensureNotNull;


@Service
public class SettingsService {

    private final SettingsDao settingsDao;

    public static final String DEFAULT_ROLES_KEY = "server.authentication.roles.default";
    private final Map<String, Setting> overridesByName;


    /**
     * Setting service allows the settings table to be interrogated.  For dev purposes then a
     * collection of overrides may be given, useful when debugging a shared database instance and
     * you do not wish to change the values in the settings table
     * @param settingsDao
     * @param overrides
     */
    @Autowired
    public SettingsService(SettingsDao settingsDao, Collection<Setting> overrides) {
        this.settingsDao = settingsDao;
        this.overridesByName = MapUtilities.indexBy(s -> s.name(), ensureNotNull(overrides));
    }


    public Collection<Setting> findAll() {
        return CollectionUtilities.map(
                settingsDao.findAll(),
                s -> Optional
                        .ofNullable(overridesByName.get(s.name()))
                        .orElse(s));
    }


    public Setting getByName(String name) {
        return Optional
                .ofNullable(overridesByName.get(name))
                .orElse(settingsDao.getByName(name));
    }

    /**
     * Helper method which looks up the setting and returns
     * it's value (or empty if either not found or the value is null)
     * @param name
     * @return
     */
    public Optional<String> getValue(String name) {
        return Optional
                .ofNullable(getByName(name))
                .flatMap(s -> s.value());
    }

}
