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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupDetail;
import com.khartec.waltz.model.app_group.AppGroupMember;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.app_group.AppGroupService;
import com.khartec.waltz.service.app_group.AppGroupSubscription;
import com.khartec.waltz.service.changelog.ChangeLogService;
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

    private static final Logger LOG = LoggerFactory.getLogger(AppGroupEndpoint.class);
    private static final String BASE_URL = mkPath("api", "app-group");

    private final AppGroupService appGroupService;
    private final ChangeLogService changeLogService;


    @Autowired
    public AppGroupEndpoint(AppGroupService service, ChangeLogService changeLogService) {
        this.appGroupService = service;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {

        String idPath = mkPath(BASE_URL, "id", ":id");

        String getByIdPath = idPath;

        String findGroupSubscriptionsForUserPath = mkPath(BASE_URL, "my-group-subscriptions");
        String findPublicGroupsPath = mkPath(BASE_URL, "public");

        String subscribePath = mkPath(idPath, "subscribe");
        String unsubscribePath = mkPath(idPath, "unsubscribe");
        String addOwnerPath = mkPath(idPath, "members", "owners");

        String deleteGroupPath = idPath;

        String addApplicationPath = mkPath(idPath, "applications");
        String removeApplicationPath = mkPath(idPath, "applications", ":applicationId");
        String updateGroupOverviewPath = idPath;


        DatumRoute<AppGroupDetail> getByIdRoute = (request, response) ->
                appGroupService.getGroupDetailById(getId(request));


        ListRoute<AppGroupSubscription> findGroupSubscriptionsRoute = (request, response) ->
                appGroupService.findGroupSubscriptionsForUser(getUsername(request));


        ListRoute<AppGroup> findPublicGroupsRoute = (request, response) ->
                appGroupService.findPublicGroups();


        ListRoute<AppGroupSubscription> subscribeRoute = (request, response) -> {
            long groupId = getId(request);
            String userId = getUsername(request);

            LOG.info("Subscribing {} to group: {}", userId, groupId);
            appGroupService.subscribe(userId, groupId);
            audit(groupId, userId, "Subscribed to group");

            return findGroupSubscriptionsRoute.apply(request, response);
        };


        ListRoute<AppGroupSubscription> unsubscribeRoute = (request, response) -> {
            long groupId = getId(request);
            LOG.info("Unsubscribing from group: {}", groupId);
            appGroupService.unsubscribe(getUsername(request), groupId);
            return findGroupSubscriptionsRoute.apply(request, response);
        };


        ListRoute<AppGroupMember> addOwnerRoute = (request, response) -> {
            String userId = getUsername(request);
            long groupId = getId(request);
            String ownerId = request.body();

            LOG.info("Adding owner: {}, to group: {}", groupId, ownerId);
            appGroupService.addOwner(userId, groupId, ownerId);
            audit(groupId, userId, String.format("Added owner %s to group %d", ownerId, groupId));

            return appGroupService.getMembers(groupId);
        };


        ListRoute<AppGroupSubscription> deleteGroupRoute = (request, response) -> {
            String userId = getUsername(request);
            long groupId = getId(request);

            LOG.warn("Deleting group: {}", groupId);
            appGroupService.deleteGroup(userId, groupId);
            audit(groupId, userId, String.format("Deleted group %d", groupId));

            return findGroupSubscriptionsRoute.apply(request, response);
        };


        ListRoute<EntityReference> addApplicationRoute = (request, response) -> {
            long groupId = getId(request);
            long applicationId = readBody(request, Long.class);
            LOG.info("Adding application: {}, to group: {} ", applicationId,  groupId);
            return appGroupService.addApplication(getUsername(request), groupId, applicationId);
        };


        ListRoute<EntityReference> removeApplicationRoute = (request, response) -> {
            long groupId = getId(request);
            long applicationId = getLong(request, "applicationId");
            LOG.info("Removing application: {}, from group: {} ", applicationId,  groupId);
            return appGroupService.removeApplication(getUsername(request), groupId, applicationId);
        };


        DatumRoute<AppGroupDetail> updateGroupOverviewRoute = (request, response) -> {
            String userId = getUsername(request);
            AppGroup appGroup = readBody(request, AppGroup.class);
            return appGroupService.updateOverview(userId, appGroup);
        };

        DatumRoute<Long> createNewGroupRoute = (request, response) -> {
            String userId = getUsername(request);
            return appGroupService.createNewGroup(userId);
        };


        getForList(findGroupSubscriptionsForUserPath, findGroupSubscriptionsRoute);

        getForDatum(getByIdPath, getByIdRoute);
        getForList(findPublicGroupsPath, findPublicGroupsRoute);

        postForList(subscribePath, subscribeRoute);
        postForList(unsubscribePath, unsubscribeRoute);
        postForList(addOwnerPath, addOwnerRoute);

        postForList(addApplicationPath, addApplicationRoute);
        deleteForList(removeApplicationPath, removeApplicationRoute);

        deleteForList(deleteGroupPath, deleteGroupRoute);
        postForDatum(updateGroupOverviewPath, updateGroupOverviewRoute);
        postForDatum(BASE_URL, createNewGroupRoute);

    }


    private void audit(long groupId, String userId, String message) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .userId(userId)
                .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                .build());
    }
}
