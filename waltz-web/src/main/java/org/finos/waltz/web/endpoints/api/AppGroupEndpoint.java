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

import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.app_group.AppGroupSubscription;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.app_group.*;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.model.entity_search.EntitySearchOptions.mkForEntity;


@Service
public class AppGroupEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AppGroupEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "app-group");


    private final AppGroupService appGroupService;


    @Autowired
    public AppGroupEndpoint(AppGroupService service) {
        this.appGroupService = service;
    }


    @Override
    public void register() {

        String idPath = WebUtilities.mkPath(BASE_URL, "id", ":id");

        String getDetailByIdPath = WebUtilities.mkPath(idPath, "detail");
        String findByIdsPath = WebUtilities.mkPath(BASE_URL, "id");

        String findGroupSubscriptionsForUserPath = WebUtilities.mkPath(BASE_URL, "my-group-subscriptions");
        String findPublicGroupsPath = WebUtilities.mkPath(BASE_URL, "public");
        String findPrivateGroupsPath = WebUtilities.mkPath(BASE_URL, "private");
        String findRelatedGroupsByEntityReferencePath = WebUtilities.mkPath(BASE_URL, "related", ":kind", ":id");

        String subscribePath = WebUtilities.mkPath(idPath, "subscribe");
        String unsubscribePath = WebUtilities.mkPath(idPath, "unsubscribe");
        String addOwnerPath = WebUtilities.mkPath(idPath, "members", "owners");
        String removeOwnerPath = WebUtilities.mkPath(idPath, "members", "owners", ":ownerId");

        String deleteGroupPath = idPath;

        String applicationListPath = WebUtilities.mkPath(idPath, "applications", "list");
        String deleteApplicationListPath = WebUtilities.mkPath(idPath, "applications", "list", "remove");
        String addApplicationPath = WebUtilities.mkPath(idPath, "applications");
        String removeApplicationPath = WebUtilities.mkPath(idPath, "applications", ":applicationId");
        String addOrgUnitPath = WebUtilities.mkPath(idPath, "orgUnits");
        String removeOrgUnitPath = WebUtilities.mkPath(idPath, "orgUnits", ":orgUnitId");
        String updateGroupOverviewPath = idPath;

        String addChangeInitiativePath = WebUtilities.mkPath(idPath, "change-initiatives");
        String removeChangeInitiativePath = WebUtilities.mkPath(idPath, "change-initiatives", ":changeInitiativeId");
        String changeInitiativeListPath = WebUtilities.mkPath(idPath, "change-initiatives", "list");
        String deleteChangeInitiativeListPath = WebUtilities.mkPath(idPath, "change-initiatives", "list", "remove");

        String searchPath = WebUtilities.mkPath(BASE_URL, "search");

        DatumRoute<AppGroupDetail> getDetailByIdRoute = (request, response) ->
                appGroupService.getGroupDetailById(WebUtilities.getId(request));

        ListRoute<AppGroup> findByIdsRoute = (request, response) ->
                appGroupService.findByIds(WebUtilities.getUsername(request), WebUtilities.readIdsFromBody(request));

        ListRoute<AppGroupSubscription> findGroupSubscriptionsRoute = (request, response) ->
                appGroupService.findGroupSubscriptionsForUser(WebUtilities.getUsername(request));

        ListRoute<AppGroup> findPublicGroupsRoute = (request, response) ->
                appGroupService.findPublicGroups();

        ListRoute<AppGroup> findPrivateGroupsRoute = (request, response) ->
                appGroupService.findPrivateGroupsByOwner(WebUtilities.getUsername(request));

        ListRoute<AppGroup> findRelatedGroupsByEntityReferenceRoute = (request, response) ->
                appGroupService.findRelatedByEntityReference(WebUtilities.getEntityReference(request), WebUtilities.getUsername(request));

        ListRoute<AppGroupSubscription> subscribeRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            String userId = WebUtilities.getUsername(request);

            LOG.info("Subscribing {} to group: {}", userId, groupId);
            appGroupService.subscribe(userId, groupId);

            return findGroupSubscriptionsRoute.apply(request, response);
        };

        ListRoute<AppGroupSubscription> unsubscribeRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            LOG.info("Unsubscribing from group: {}", groupId);
            appGroupService.unsubscribe(WebUtilities.getUsername(request), groupId);
            return findGroupSubscriptionsRoute.apply(request, response);
        };

        ListRoute<AppGroupMember> addOwnerRoute = (request, response) -> {
            String userId = WebUtilities.getUsername(request);
            long groupId = WebUtilities.getId(request);
            String ownerId = request.body();

            LOG.info("Adding owner: {}, to group: {}", groupId, ownerId);
            appGroupService.addOwner(userId, groupId, ownerId);

            return appGroupService.getMembers(groupId);
        };

        ListRoute<AppGroupMember> removeOwnerRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            String ownerToRemoveId = request.params("ownerId"); // get email Of owner
            String requestorName = WebUtilities.getUsername(request); // get userId of requestor

            LOG.info("Removing owner: {} from app group: {}", ownerToRemoveId, groupId);
            appGroupService.removeOwner(requestorName, groupId, ownerToRemoveId);

            return appGroupService.getMembers(groupId);
        };

        ListRoute<AppGroupSubscription> deleteGroupRoute = (request, response) -> {
            String userId = WebUtilities.getUsername(request);
            long groupId = WebUtilities.getId(request);

            LOG.warn("Deleting group: {}", groupId);
            appGroupService.deleteGroup(userId, groupId);

            return findGroupSubscriptionsRoute.apply(request, response);
        };

        ListRoute<AppGroupEntry> addApplicationRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long applicationId = WebUtilities.readBody(request, Long.class);
            LOG.info("Adding application: {}, to group: {} ", applicationId,  groupId);
            return appGroupService.addApplication(
                    WebUtilities.getUsername(request),
                    groupId,
                    applicationId);
        };

        ListRoute<AppGroupEntry> addApplicationListRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            AppGroupBulkAddRequest appGroupBulkAddRequest = WebUtilities.readBody(request, AppGroupBulkAddRequest.class);
            LOG.info("Adding applications: {}, to group: {} ", appGroupBulkAddRequest.applicationIds(),  groupId);
            String userId = WebUtilities.getUsername(request);
            return appGroupService.addApplications(
                    userId,
                    groupId,
                    appGroupBulkAddRequest.applicationIds(),
                    appGroupBulkAddRequest.unknownIdentifiers());
        };

        ListRoute<AppGroupEntry> removeApplicationRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long applicationId = WebUtilities.getLong(request, "applicationId");
            LOG.info("Removing application: {}, from group: {} ", applicationId,  groupId);
            return appGroupService.removeApplication(WebUtilities.getUsername(request), groupId, applicationId);
        };

        ListRoute<AppGroupEntry> addOrgUnitRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long orgUnitId = WebUtilities.readBody(request, Long.class);
            LOG.info("Adding orgUnit: {}, to application group: {} ", orgUnitId,  groupId);
            return appGroupService.addOrganisationalUnit(WebUtilities.getUsername(request), groupId, orgUnitId);
        };

        ListRoute<AppGroupEntry> removeOrgUnitRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long orgUnitId = WebUtilities.getLong(request, "orgUnitId");
            LOG.info("Removing orgUnit: {}, from application group: {} ", orgUnitId,  groupId);
            return appGroupService.removeOrganisationalUnit(WebUtilities.getUsername(request), groupId, orgUnitId);
        };

        ListRoute<AppGroupEntry> removeApplicationListRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            List<Long> applicationIds = WebUtilities.readIdsFromBody(request);
            LOG.info("Removing applications: {}, from group: {} ", applicationIds,  groupId);
            String userId = WebUtilities.getUsername(request);
            return appGroupService.removeApplications(userId, groupId, applicationIds);
        };

        DatumRoute<AppGroupDetail> updateGroupOverviewRoute = (request, response) -> {
            String userId = WebUtilities.getUsername(request);
            AppGroup appGroup = WebUtilities.readBody(request, AppGroup.class);
            return appGroupService.updateOverview(userId, appGroup);
        };

        DatumRoute<Long> createNewGroupRoute = (request, response) -> {
            String userId = WebUtilities.getUsername(request);
            return appGroupService.createNewGroup(userId);
        };

        ListRoute<AppGroupEntry> addChangeInitiativeRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long changeInitiativeId = WebUtilities.readBody(request, Long.class);
            LOG.info("Adding Change Initiative: {}, to group: {} ", changeInitiativeId, groupId);
            return appGroupService.addChangeInitiative(WebUtilities.getUsername(request), groupId, changeInitiativeId);
        };

        ListRoute<AppGroupEntry> removeChangeInitiativeRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            long changeInitiativeId = WebUtilities.getLong(request, "changeInitiativeId");
            LOG.info("Removing Change Initiative: {}, from group: {} ", changeInitiativeId, groupId);
            return appGroupService.removeChangeInitiative(WebUtilities.getUsername(request), groupId, changeInitiativeId);
        };


        ListRoute<AppGroupEntry> addChangeInitiativeListRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            AppGroupBulkAddRequest appGroupBulkAddRequest = WebUtilities.readBody(request, AppGroupBulkAddRequest.class);
            LOG.info("Adding change initiatives: {}, to group: {} ", appGroupBulkAddRequest.changeInitiativeIds(), groupId);
            String userId = WebUtilities.getUsername(request);
            return appGroupService.addChangeInitiatives(
                    userId,
                    groupId,
                    appGroupBulkAddRequest.changeInitiativeIds());
        };

        ListRoute<AppGroupEntry> removeChangeInitiativeListRoute = (request, response) -> {
            long groupId = WebUtilities.getId(request);
            List<Long> changeInitiativeIds = WebUtilities.readIdsFromBody(request);
            LOG.info("Removing change initiatives: {}, from group: {} ", changeInitiativeIds, groupId);
            String userId = WebUtilities.getUsername(request);
            return appGroupService.removeChangeInitiatives(userId, groupId, changeInitiativeIds);
        };


        ListRoute<AppGroup> searchRoute = (request, response) ->
                appGroupService.search(mkForEntity(EntityKind.APP_GROUP, WebUtilities.readBody(request, String.class)));


        EndpointUtilities.getForList(findGroupSubscriptionsForUserPath, findGroupSubscriptionsRoute);

        EndpointUtilities.getForDatum(getDetailByIdPath, getDetailByIdRoute);
        EndpointUtilities.postForList(findByIdsPath, findByIdsRoute);
        EndpointUtilities.getForList(findPublicGroupsPath, findPublicGroupsRoute);
        EndpointUtilities.getForList(findPrivateGroupsPath, findPrivateGroupsRoute);
        EndpointUtilities.getForList(findRelatedGroupsByEntityReferencePath, findRelatedGroupsByEntityReferenceRoute);

        EndpointUtilities.postForList(subscribePath, subscribeRoute);
        EndpointUtilities.postForList(unsubscribePath, unsubscribeRoute);
        EndpointUtilities.postForList(addOwnerPath, addOwnerRoute);
        EndpointUtilities.deleteForList(removeOwnerPath, removeOwnerRoute);

        EndpointUtilities.postForList(addApplicationPath, addApplicationRoute);
        EndpointUtilities.postForList(applicationListPath, addApplicationListRoute);
        EndpointUtilities.deleteForList(removeApplicationPath, removeApplicationRoute);
        EndpointUtilities.postForList(deleteApplicationListPath, removeApplicationListRoute);

        EndpointUtilities.postForList(addOrgUnitPath, addOrgUnitRoute);
        EndpointUtilities.deleteForList(removeOrgUnitPath, removeOrgUnitRoute);

        EndpointUtilities.postForList(addChangeInitiativePath, addChangeInitiativeRoute);
        EndpointUtilities.deleteForList(removeChangeInitiativePath, removeChangeInitiativeRoute);

        EndpointUtilities.postForList(changeInitiativeListPath, addChangeInitiativeListRoute);
        EndpointUtilities.postForList(deleteChangeInitiativeListPath, removeChangeInitiativeListRoute);

        EndpointUtilities.deleteForList(deleteGroupPath, deleteGroupRoute);
        EndpointUtilities.postForDatum(updateGroupOverviewPath, updateGroupOverviewRoute);
        EndpointUtilities.postForDatum(BASE_URL, createNewGroupRoute);

        EndpointUtilities.postForList(searchPath, searchRoute);
    }

}
