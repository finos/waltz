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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.model.user.Role;
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
        return userRoleService.hasRole(getUsername(request), Role.ADMIN);
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
