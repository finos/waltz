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

package com.khartec.waltz.web.endpoints.auth;

import com.khartec.waltz.model.settings.NamedSettings;
import com.khartec.waltz.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.StringUtilities.notEmpty;


/**
 * Simple filter which works by reading a username from the
 * http headers of a request.  The header name is either 'remote-user'
 * or is specified by a settings: 'server.authentication.filter.headerbased.param'
 */
public class HeaderBasedAuthenticationFilter extends WaltzFilter {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderBasedAuthenticationFilter.class);

    private final String paramName;


    public HeaderBasedAuthenticationFilter(SettingsService settingsService) {
        super(settingsService);

        paramName = getSettingValue(NamedSettings.headerBasedAuthenticationFilterParam)
                .orElseGet(() -> {
                    LOG.warn("HeaderBasedAuthenticationFilter is configured but no header parameter give, therefore defaulting.");
                    return "remote-user";
                });

        LOG.info("Using header param: '" + paramName + "' for authentication purposes");
    }


    @Override
    public void handle(Request request,
                       Response response) throws Exception {
        String userParam = request.headers(paramName);
        LOG.debug("User according to header: "+userParam);
        if (notEmpty(userParam)) {
            AuthenticationUtilities.setUser(request, userParam);
        } else {
            AuthenticationUtilities.setUserAsAnonymous(request);
        }
    }

}
