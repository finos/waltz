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

import com.khartec.waltz.data.settings.SettingsDao;
import com.khartec.waltz.model.settings.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;


@Service
public class SettingsService {

    private final SettingsDao settingsDao;

    public static final String DEFAULT_ROLES_KEY = "server.authentication.roles.default";


    @Autowired
    public SettingsService(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }


    public Collection<Setting> findAll() {
        return settingsDao.findAll();
    }


    public Setting getByName(String name) {
        return settingsDao.getByName(name);
    }

    /**
     * Helper method which looks up the setting and returns
     * it's value (or empty if either not found or the value is null)
     * @param name
     * @return
     */
    public Optional<String> getValue(String name) {
        Setting setting = settingsDao.getByName(name);
        return setting == null
            ? Optional.empty()
            : setting.value();
    }

}
