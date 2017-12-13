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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.user_agent_info.ImmutableUserAgentInfo;
import com.khartec.waltz.model.user_agent_info.UserAgentInfo;
import com.khartec.waltz.service.user_agent_info.UserAgentInfoService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.BrowserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

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
