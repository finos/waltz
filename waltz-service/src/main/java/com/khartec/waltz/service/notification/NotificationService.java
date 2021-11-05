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

package com.khartec.waltz.service.notification;


import org.finos.waltz.data.notification.NotificationDao;
import com.khartec.waltz.model.notification.ImmutableNotificationResponse;
import com.khartec.waltz.model.notification.NotificationResponse;
import com.khartec.waltz.model.notification.NotificationSummary;
import com.khartec.waltz.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class NotificationService {

    private final NotificationDao notificationDao;
    private final SettingsService settingsService;
    private static final String NOTIFICATION_MESSAGE_KEY = "ui.banner.notification.text";


    @Autowired
    public NotificationService(NotificationDao notificationDao, SettingsService settingsService) {
        checkNotNull(notificationDao, "notificationDao cannot be null");

        this.notificationDao = notificationDao;
        this.settingsService = settingsService;
    }


    public NotificationResponse getNotificationsByUserId(String userId) {
        List<NotificationSummary> summary = notificationDao.findNotificationsByUserId(userId);
        Optional<String> message = settingsService.getValue(NOTIFICATION_MESSAGE_KEY);

        return ImmutableNotificationResponse.builder()
                .summary(summary)
                .message(message)
                .build();
    }

}
