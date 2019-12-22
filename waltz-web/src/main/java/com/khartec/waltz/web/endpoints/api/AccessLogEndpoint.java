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
import static com.khartec.waltz.web.WebUtilities.*;
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
        String writePath = mkPath(BASE_URL, ":state");

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
                .params(request.body())
                .build();

        accessLogService.write(accessLog);

        return waltzVersionInfo;
    }

}
