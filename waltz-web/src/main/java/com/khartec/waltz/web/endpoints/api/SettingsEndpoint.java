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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.settings.SettingsService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.Collection;

import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.web.WebUtilities.getUsername;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

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


        ListRoute<Setting> findAllRoute = (request, response) -> {
            Collection<Setting> settings = settingsService.findAll();
            return isAdmin(request) ? settings : sanitize(settings);
        };


        DatumRoute<Setting> getByNameRoute = (request, response) -> {
            String name = request.params("name");
            Setting setting = settingsService.getByName(name);
            return isAdmin(request) ? setting : sanitize(setting);
        };


        getForList(findAllPath, findAllRoute);
        getForDatum(getByNamePath, getByNameRoute);
    }


    private boolean isAdmin(Request request) {
        return userRoleService.hasRole(getUsername(request), SystemRole.ADMIN);
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
