package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.notification.NotificationSummary;
import com.khartec.waltz.service.notification.NotificationService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getUsername;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


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

        String findNotificationsByUserIdPath = mkPath(BASE_URL);

        ListRoute<NotificationSummary> findNotificationsByUserIdRoute = (request, response)
                -> notificationService.findNotificationsByUserId(getUsername(request));

        getForList(findNotificationsByUserIdPath, findNotificationsByUserIdRoute);
    }

}
