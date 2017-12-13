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

import com.khartec.waltz.model.WaltzVersionInfo;
import com.khartec.waltz.model.accesslog.AccessLog;
import com.khartec.waltz.model.accesslog.AccessTime;
import com.khartec.waltz.model.accesslog.ImmutableAccessLog;
import com.khartec.waltz.service.access_log.AccessLogService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getLimit;
import static com.khartec.waltz.web.WebUtilities.getUsername;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class AccessLogEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "access-log");

    private final AccessLogService accessLogService;
    private final WaltzVersionInfo waltzVersionInfo;


    @Autowired
    public AccessLogEndpoint(AccessLogService accessLogService,
                             WaltzVersionInfo waltzVersionInfo) {
        checkNotNull(accessLogService, "accessLogService cannot be null");
        checkNotNull(waltzVersionInfo, "waltzVersionInfo cannot be null");

        this.accessLogService = accessLogService;
        this.waltzVersionInfo = waltzVersionInfo;
    }


    @Override
    public void register() {

        String findForUserPath = mkPath(BASE_URL, "user", ":userId");
        String findActiveUsersPath = mkPath(BASE_URL, "active", ":minutes");
        String writePath = mkPath(BASE_URL, ":state", ":params");

        ListRoute<AccessLog> findForUserRoute = (request, response) ->
                accessLogService.findForUserId(request.params("userId"), getLimit(request));
        ListRoute<AccessTime> findActiveUsersRoute = (request, response) -> {
            java.time.Duration minutes = java.time.Duration.ofMinutes(Integer.parseInt(request.params("minutes")));
            return accessLogService.findActiveUsersSince(minutes);
        };

        getForList(findForUserPath, findForUserRoute);
        getForList(findActiveUsersPath, findActiveUsersRoute);
        postForDatum(writePath, this::writeRoute);
    }


    private WaltzVersionInfo writeRoute(Request request, Response response) {
        AccessLog accessLog = ImmutableAccessLog.builder()
                .userId(getUsername(request))
                .state(request.params("state"))
                .params(request.params("params"))
                .build();

        accessLogService.write(accessLog);

        return waltzVersionInfo;
    }

}
