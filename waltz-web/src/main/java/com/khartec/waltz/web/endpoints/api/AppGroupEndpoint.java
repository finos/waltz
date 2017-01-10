/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupDetail;
import com.khartec.waltz.model.app_group.AppGroupMember;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.service.app_group.AppGroupService;
import com.khartec.waltz.service.app_group.AppGroupSubscription;
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


    @Autowired
    public AppGroupEndpoint(AppGroupService service) {
        this.appGroupService = service;
    }


    @Override
    public void register() {

        String idPath = mkPath(BASE_URL, "id", ":id");

        String getDetailByIdPath = mkPath(idPath, "detail");
        String findByIdsPath = mkPath(BASE_URL, "id");

        String findGroupSubscriptionsForUserPath = mkPath(BASE_URL, "my-group-subscriptions");
        String findPublicGroupsPath = mkPath(BASE_URL, "public");

        String subscribePath = mkPath(idPath, "subscribe");
        String unsubscribePath = mkPath(idPath, "unsubscribe");
        String addOwnerPath = mkPath(idPath, "members", "owners");

        String deleteGroupPath = idPath;

        String addApplicationPath = mkPath(idPath, "applications");
        String removeApplicationPath = mkPath(idPath, "applications", ":applicationId");
        String updateGroupOverviewPath = idPath;

        String addChangeInitiativePath = mkPath(idPath, "change-initiatives");
        String removeChangeInitiativePath = mkPath(idPath, "change-initiatives", ":changeInitiativeId");


        DatumRoute<AppGroupDetail> getDetailByIdRoute = (request, response) ->
                appGroupService.getGroupDetailById(getId(request));

        ListRoute<AppGroup> findByIdsRoute = (request, response) ->
                appGroupService.findByIds(getUsername(request), readIdsFromBody(request));

        ListRoute<AppGroupSubscription> findGroupSubscriptionsRoute = (request, response) ->
                appGroupService.findGroupSubscriptionsForUser(getUsername(request));

        ListRoute<AppGroup> findPublicGroupsRoute = (request, response) ->
                appGroupService.findPublicGroups();

        ListRoute<AppGroupSubscription> subscribeRoute = (request, response) -> {
            long groupId = getId(request);
            String userId = getUsername(request);

            LOG.info("Subscribing {} to group: {}", userId, groupId);
            appGroupService.subscribe(userId, groupId);

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

            return appGroupService.getMembers(groupId);
        };

        ListRoute<AppGroupSubscription> deleteGroupRoute = (request, response) -> {
            String userId = getUsername(request);
            long groupId = getId(request);

            LOG.warn("Deleting group: {}", groupId);
            appGroupService.deleteGroup(userId, groupId);

            return findGroupSubscriptionsRoute.apply(request, response);
        };

        ListRoute<EntityReference> addApplicationRoute = (request, response) -> {
            long groupId = getId(request);
            long applicationId = readBody(request, Long.class);
            LOG.info("Adding application: {}, to group: {} ", applicationId,  groupId);
            String userId = getUsername(request);
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
            Long groupId = appGroupService.createNewGroup(userId);
            return groupId;
        };

        ListRoute<ChangeInitiative> addChangeInitiativeRoute = (request, response) -> {
            long groupId = getId(request);
            long changeInitiativeId = readBody(request, Long.class);
            LOG.info("Adding Change Initiative: {}, to group: {} ", changeInitiativeId,  groupId);
            return appGroupService.addChangeInitiative(getUsername(request), groupId, changeInitiativeId);
        };

        ListRoute<ChangeInitiative> removeChangeInitiativeRoute = (request, response) -> {
            long groupId = getId(request);
            long changeInitiativeId = getLong(request, "changeInitiativeId");
            LOG.info("Removing Change Initiative: {}, from group: {} ", changeInitiativeId,  groupId);
            return appGroupService.removeChangeInitiative(getUsername(request), groupId, changeInitiativeId);
        };


        getForList(findGroupSubscriptionsForUserPath, findGroupSubscriptionsRoute);

        getForDatum(getDetailByIdPath, getDetailByIdRoute);
        postForList(findByIdsPath, findByIdsRoute);
        getForList(findPublicGroupsPath, findPublicGroupsRoute);

        postForList(subscribePath, subscribeRoute);
        postForList(unsubscribePath, unsubscribeRoute);
        postForList(addOwnerPath, addOwnerRoute);

        postForList(addApplicationPath, addApplicationRoute);
        deleteForList(removeApplicationPath, removeApplicationRoute);

        postForList(addChangeInitiativePath, addChangeInitiativeRoute);
        deleteForList(removeChangeInitiativePath, removeChangeInitiativeRoute);

        deleteForList(deleteGroupPath, deleteGroupRoute);
        postForDatum(updateGroupOverviewPath, updateGroupOverviewRoute);
        postForDatum(BASE_URL, createNewGroupRoute);

    }

}
