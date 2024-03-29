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

import org.finos.waltz.service.notification.NotificationService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.notification.NotificationResponse;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class NotificationEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "notification");

    private final NotificationService notificationService;


    @Autowired
    public NotificationEndpoint(NotificationService notificationService) {
        checkNotNull(notificationService, "notificationService cannot be null");

        this.notificationService = notificationService;
    }


    @Override
    public void register() {

        String getNotificationsByUserIdPath = WebUtilities.mkPath(BASE_URL);

        DatumRoute<NotificationResponse> getNotificationsByUserIdRoute = (request, response)
                -> notificationService.getNotificationsByUserId(WebUtilities.getUsername(request));

        EndpointUtilities.getForDatum(getNotificationsByUserIdPath, getNotificationsByUserIdRoute);
    }

}
