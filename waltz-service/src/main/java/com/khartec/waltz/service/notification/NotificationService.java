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
