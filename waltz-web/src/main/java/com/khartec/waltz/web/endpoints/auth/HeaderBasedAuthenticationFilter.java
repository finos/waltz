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
