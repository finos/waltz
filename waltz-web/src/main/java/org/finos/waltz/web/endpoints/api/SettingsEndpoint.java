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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.settings.UpdateSettingsCommand;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.Collection;

import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class SettingsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "settings");

    private final SettingsService settingsService;
    private final UserRoleService userRoleService;


    @Autowired
    public SettingsEndpoint(SettingsService settingsService,
                            UserRoleService userRoleService) {
        this.settingsService = settingsService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL);
        String getByNamePath = mkPath(BASE_URL, "name", ":name");
        String updateValuePath = mkPath(BASE_URL, "update");
        String createValuePath = mkPath(BASE_URL, "create");


        ListRoute<Setting> findAllRoute = (request, response) -> {
            Collection<Setting> settings = settingsService.findAll();
            return isAdmin(request) ? settings : sanitize(settings);
        };


        DatumRoute<Setting> getByNameRoute = (request, response) -> {
            String name = request.params("name");
            Setting setting = settingsService.getByName(name);
            return isAdmin(request) ? setting : sanitize(setting);
        };

        DatumRoute<Integer> updateValueRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.ADMIN);
            UpdateSettingsCommand updateCommand = readBody(request, UpdateSettingsCommand.class);
            return settingsService.update(updateCommand);
        };

        DatumRoute<Integer> createRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.ADMIN);
            Setting setting = readBody(request, Setting.class);
            return settingsService.create(setting);
        };


        getForList(findAllPath, findAllRoute);
        getForDatum(getByNamePath, getByNameRoute);
        postForDatum(updateValuePath, updateValueRoute);
        postForDatum(createValuePath, createRoute);
    }


    private boolean isAdmin(Request request) {
        return userRoleService.hasRole(WebUtilities.getUsername(request), SystemRole.ADMIN);
    }


    private Collection<Setting> sanitize(Collection<Setting> settings) {
        return map(settings, s -> sanitize(s));
    }


    private Setting sanitize(Setting s) {
        return s.restricted()
                ? ImmutableSetting.copyOf(s).withValue("****")
                : s;
    }

}
