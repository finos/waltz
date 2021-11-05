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

import org.finos.waltz.service.user_agent_info.UserAgentInfoService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.BrowserInfo;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.user_agent_info.ImmutableUserAgentInfo;
import org.finos.waltz.model.user_agent_info.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class UserAgentInfoEndpoint implements Endpoint {


    private static final String BASE_URL = mkPath("api", "user-agent-info");
    private static final int DEFAULT_LIMIT = 10;

    private final UserAgentInfoService userAgentInfoService;


    @Autowired
    public UserAgentInfoEndpoint(UserAgentInfoService userAgentInfoService) {
        checkNotNull(userAgentInfoService, "userAgentInfoService cannot be null");
        this.userAgentInfoService = userAgentInfoService;
    }


    @Override
    public void register() {
        String findForUserPath = mkPath(BASE_URL, "user", ":userName");
        String recordLoginPath = mkPath(BASE_URL);

        ListRoute<UserAgentInfo> findForUserRoute = ((request, response) -> {
            String userName = request.params("userName");
            String limitStr = request.queryParams("limit");
            int limit = limitStr == null || limitStr.equals("")
                    ? DEFAULT_LIMIT
                    : Integer.parseInt(limitStr);
            
            return userAgentInfoService.findLoginsForUser(userName, limit);
        });

        DatumRoute<Integer> recordLoginRoute = (request, response) -> {
            BrowserInfo browserInfo = readBody(request, BrowserInfo.class);
            UserAgentInfo userAgentInfo = ImmutableUserAgentInfo.builder()
                    .userName(getUsername(request))
                    .userAgent(request.userAgent())
                    .operatingSystem(browserInfo.operatingSystem())
                    .resolution(browserInfo.resolution())
                    .loginTimestamp(DateTimeUtilities.nowUtc())
                    .ipAddress(request.ip())
                    .build();

            return userAgentInfoService.save(userAgentInfo);
        };

        getForList(findForUserPath, findForUserRoute);
        postForDatum(recordLoginPath, recordLoginRoute);

    }
}
