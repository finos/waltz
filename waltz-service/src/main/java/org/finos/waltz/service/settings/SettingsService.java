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

package org.finos.waltz.service.settings;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.settings.SettingsDao;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.model.settings.UpdateSettingsCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.ensureNotNull;


@Service
public class SettingsService {

    private final SettingsDao settingsDao;

    public static final String DEFAULT_ROLES_KEY = "server.authentication.roles.default";
    public static final String ALLOW_COST_EXPORTS_KEY = "feature.data-extractor.entity-cost.enabled";

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


    public Map<String, String> indexByPrefix(String prefix) {
        return settingsDao.indexByPrefix(prefix);
    }


    public int update(UpdateSettingsCommand cmd) {
        return settingsDao.update(cmd);
    }

    public Integer create(Setting setting) {
        return settingsDao.create(setting);
    }
}
