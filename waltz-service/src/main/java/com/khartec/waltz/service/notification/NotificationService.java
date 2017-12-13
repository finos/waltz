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

package com.khartec.waltz.service.notification;


import com.khartec.waltz.data.notification.NotificationDao;
import com.khartec.waltz.model.notification.NotificationSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class NotificationService {

    private final NotificationDao notificationDao;


    @Autowired
    public NotificationService(NotificationDao notificationDao) {
        checkNotNull(notificationDao, "notificationDao cannot be null");

        this.notificationDao = notificationDao;
    }


    public List<NotificationSummary> findNotificationsByUserId(String userId) {
        return notificationDao.findNotificationsByUserId(userId);
    }

}
