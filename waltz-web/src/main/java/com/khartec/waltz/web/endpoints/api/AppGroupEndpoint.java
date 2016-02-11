/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupDetail;
import com.khartec.waltz.service.app_group.AppGroupSubscription;
import com.khartec.waltz.service.app_group.AppGroupService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class AppGroupEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEndpoint.class);
    private static final String BASE_URL = mkPath("api", "app-group");

    private final AppGroupService service;


    @Autowired
    public AppGroupEndpoint(AppGroupService service) {
        this.service = service;
    }


    @Override
    public void register() {

        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findGroupSubscriptionsForUserPath = mkPath(BASE_URL, "my-group-subscriptions");
        String findPublicGroupsPath = mkPath(BASE_URL, "public");
        String subscribePath = mkPath(BASE_URL, "id", ":id", "subscribe");

        DatumRoute<AppGroupDetail> getByIdRoute = (request, response) ->
                service.getGroupDetailById(getId(request));

        ListRoute<AppGroupSubscription> findGroupSubscriptionsRoute = (request, response) ->
                service.findGroupSubscriptionsForUser(getUser(request).userName());

        ListRoute<AppGroup> findPublicGroupsRoute = (request, response) ->
                service.findPublicGroups();

        ListRoute<AppGroupSubscription> subscribeRoute = (request, response) -> {
            long groupId = getId(request);
            service.subscribe(getUser(request).userName(), groupId);
            LOG.info("Subscribing to group: " + groupId);
            return findGroupSubscriptionsRoute.apply(request, response);
        };

        ListRoute<AppGroupSubscription> unsubscribeRoute = (request, response) -> {
            long groupId = getId(request);
            service.unsubscribe(getUser(request).userName(), groupId);
            LOG.info("Unsubscribing from group: " + groupId);
            return findGroupSubscriptionsRoute.apply(request, response);
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findGroupSubscriptionsForUserPath, findGroupSubscriptionsRoute);
        getForList(findPublicGroupsPath, findPublicGroupsRoute);
        postForList(subscribePath, subscribeRoute);
    }
}
