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

package com.khartec.waltz.service.app_group;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.app_group.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class AppGroupService {

    private final AppGroupDao appGroupDao;
    private final AppGroupMemberDao appGroupMemberDao;
    private final AppGroupEntryDao appGroupEntryDao;
    private final ApplicationDao applicationDao;
    private final EntityRelationshipDao entityRelationshipDao;
    private final ChangeInitiativeService changeInitiativeService;
    private final ChangeLogService changeLogService;


    @Autowired
    public AppGroupService(AppGroupDao appGroupDao,
                           AppGroupMemberDao appGroupMemberDao,
                           AppGroupEntryDao appGroupEntryDao,
                           ApplicationDao applicationDao,
                           EntityRelationshipDao entityRelationshipDao,
                           ChangeInitiativeService changeInitiativeService,
                           ChangeLogService changeLogService) {
        checkNotNull(appGroupDao, "appGroupDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        checkNotNull(changeInitiativeService, "changeInitiativeService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appGroupDao = appGroupDao;
        this.appGroupMemberDao = appGroupMemberDao;
        this.appGroupEntryDao = appGroupEntryDao;
        this.applicationDao = applicationDao;
        this.entityRelationshipDao = entityRelationshipDao;
        this.changeInitiativeService = changeInitiativeService;
        this.changeLogService = changeLogService;
    }


    public AppGroupDetail getGroupDetailById(long groupId) {
        return ImmutableAppGroupDetail.builder()
                .appGroup(appGroupDao.getGroup(groupId))
                .applications(appGroupEntryDao.getEntriesForGroup(groupId))
                .members(appGroupMemberDao.getMembers(groupId))
                .build();
    }


    public List<AppGroupSubscription> findGroupSubscriptionsForUser(String userId) {
        List<AppGroupMember> subscriptions = appGroupMemberDao.getSubscriptions(userId);
        Map<Long, AppGroupMemberRole> roleByGroup = indexBy(
                m -> m.groupId(),
                m -> m.role(),
                subscriptions);

        List<AppGroup> groups = appGroupDao.findGroupsForUser(userId);

        return groups
                .stream()
                .map(g -> ImmutableAppGroupSubscription.builder()
                        .appGroup(g)
                        .role(roleByGroup.get(g.id().get()))
                        .build())
                .collect(Collectors.toList());
    }


    public List<AppGroup> findPublicGroups() {
        return appGroupDao.findPublicGroups();
    }


    public List<AppGroup> findPrivateGroupsByOwner(String ownerId) {
        return appGroupDao.findPrivateGroupsByOwner(ownerId);
    }


    public List<AppGroup> findRelatedByEntityReferenceAndUser(EntityReference ref, String userId) {
        switch (ref.kind()) {
            case APPLICATION:
                return appGroupDao.findRelatedByApplicationAndUser(ref.id(), userId);
            default:
                return appGroupDao.findRelatedByEntityReferenceAndUser(ref, userId);
        }
    }


    public List<AppGroup> search(String terms, EntitySearchOptions options) {
        checkNotNull(terms, "terms cannot be null");
        checkNotNull(options, "options cannot be null");

        return appGroupDao.search(terms, options);
    }


    public void subscribe(String userId, long groupId) {
        audit(groupId, userId, "Subscribed to group", EntityKind.PERSON, Operation.ADD);
        appGroupMemberDao.register(groupId, userId);
    }


    public void unsubscribe(String userId, long groupId) {
        audit(groupId, userId, "Unsubscribed from group", EntityKind.PERSON, Operation.REMOVE);
        appGroupMemberDao.unregister(groupId, userId);
    }


    public List<AppGroupSubscription> deleteGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupDao.deleteGroup(groupId);
        entityRelationshipDao.removeAnyInvolving(mkRef(EntityKind.APP_GROUP, groupId));
        audit(groupId, userId, String.format("Removed group %d", groupId), null, Operation.REMOVE);
        return findGroupSubscriptionsForUser(userId);
    }


    public List<EntityReference> addApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {

        verifyUserCanUpdateGroup(userId, groupId);

        Application app = applicationDao.getById(applicationId);
        if (app != null) {
            appGroupEntryDao.addApplication(groupId, applicationId);
            audit(groupId, userId, String.format("Added application %s to group", app.name()), EntityKind.APPLICATION, Operation.ADD);
        }

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> addApplications(String userId, long groupId, List<Long> applicationIds) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);

        appGroupEntryDao.addApplications(groupId, applicationIds);

        List<Application> apps = applicationDao.findByIds(applicationIds);
        List<ChangeLog> changeLogs = apps
                .stream()
                .map(app -> ImmutableChangeLog.builder()
                        .message(String.format("Added application %s to group", app.name()))
                        .userId(userId)
                        .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                        .childKind(EntityKind.APPLICATION)
                        .operation(Operation.ADD)
                        .build())
                .collect(Collectors.toList());
        changeLogService.write(changeLogs);

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> removeApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupEntryDao.removeApplication(groupId, applicationId);
        Application app = applicationDao.getById(applicationId);
        audit(groupId, userId, String.format(
                    "Removed application %s from group",
                    app != null
                        ? app.name()
                        : applicationId),
                EntityKind.APPLICATION,
                Operation.REMOVE);
        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> removeApplications(String userId, long groupId, List<Long> applicationIds) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);

        appGroupEntryDao.removeApplications(groupId, applicationIds);

        List<Application> apps = applicationDao.findByIds(applicationIds);
        List<ChangeLog> changeLogs = apps
                .stream()
                .map(app -> ImmutableChangeLog.builder()
                        .message(String.format("Removed application %s from group", app.name()))
                        .userId(userId)
                        .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                        .childKind(EntityKind.APPLICATION)
                        .operation(Operation.REMOVE)
                        .build())
                .collect(Collectors.toList());
        changeLogService.write(changeLogs);

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public int addOwner(String userId, long groupId, String ownerId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        audit(groupId, userId, String.format("Added owner %s to group %d", ownerId, groupId), EntityKind.PERSON, Operation.ADD);
        return appGroupMemberDao.register(groupId, ownerId, AppGroupMemberRole.OWNER);
    }


    public boolean removeOwner(String userId, long groupId, String ownerToRemoveId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        boolean result = appGroupMemberDao.unregister(groupId, ownerToRemoveId);
        subscribe(ownerToRemoveId, groupId);
        audit(groupId, userId, String.format("Removed owner %s from group %d", ownerToRemoveId, groupId), EntityKind.PERSON, Operation.REMOVE);
        return result;
    }


    public List<AppGroupMember> getMembers(long groupId) {
        return appGroupMemberDao.getMembers(groupId);
    }


    public AppGroupDetail updateOverview(String userId, AppGroup appGroup) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, appGroup.id().get());
        appGroupDao.update(appGroup);
        audit(appGroup.id().get(), userId, "Updated group overview", null, Operation.UPDATE);
        return getGroupDetailById(appGroup.id().get());
    }


    public Long createNewGroup(String userId) {
        long groupId = appGroupDao.insert(ImmutableAppGroup.builder()
                .description("New group created by: " + userId)
                .name("New group created by: "+userId)
                .appGroupKind(AppGroupKind.PRIVATE)
                .build());

        appGroupMemberDao.register(groupId, userId, AppGroupMemberRole.OWNER);

        audit(groupId, userId, "Created group", null, Operation.ADD);


        return groupId;
    }


    public Collection<AppGroup> findByIds(String user, List<Long> ids) {
        Checks.checkNotEmpty(user, "user cannot be empty");
        checkNotNull(ids, "ids cannot be null");
        return appGroupDao.findByIds(user, ids);
    }


    public Collection<ChangeInitiative> addChangeInitiative(
            String username,
            long groupId,
            long changeInitiativeId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(username, groupId);

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(username, groupId, changeInitiativeId);
        entityRelationshipDao.save(entityRelationship);

        audit(groupId,
                username,
                String.format("Associated change initiative: %d", changeInitiativeId),
                EntityKind.CHANGE_INITIATIVE,
                Operation.ADD);

        return changeInitiativeService.findForSelector(mkOpts(
                mkRef(EntityKind.APP_GROUP, groupId),
                HierarchyQueryScope.EXACT));
    }


    public Collection<ChangeInitiative> removeChangeInitiative(
            String username,
            long groupId,
            long changeInitiativeId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(username, groupId);

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(username, groupId, changeInitiativeId);
        entityRelationshipDao.remove(entityRelationship);

        audit(groupId,
                username,
                String.format("Removed associated change initiative: %d", changeInitiativeId),
                EntityKind.CHANGE_INITIATIVE,
                Operation.REMOVE);

        return changeInitiativeService.findForSelector(mkOpts(
                mkRef(EntityKind.APP_GROUP, groupId),
                HierarchyQueryScope.EXACT));
    }


    private void verifyUserCanUpdateGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        if (!appGroupMemberDao.canUpdate(groupId, userId)) {
            throw new InsufficientPrivelegeException(userId + " cannot update group: " + groupId);
        }
    }


    private EntityRelationship buildChangeInitiativeRelationship(String username, long groupId, long changeInitiativeId) {
        EntityReference appGroupRef = ImmutableEntityReference.builder()
                .kind(EntityKind.APP_GROUP)
                .id(groupId)
                .build();

        EntityReference changeInitiativeRef = ImmutableEntityReference.builder()
                .kind(EntityKind.CHANGE_INITIATIVE)
                .id(changeInitiativeId)
                .build();

        return ImmutableEntityRelationship.builder()
                .a(appGroupRef)
                .b(changeInitiativeRef)
                .relationship(RelationshipKind.RELATES_TO)
                .lastUpdatedBy(username)
                .provenance("waltz")
                .build();
    }



    private void audit(long groupId, String userId, String message, EntityKind childKind, Operation operation) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .userId(userId)
                .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                .childKind(Optional.ofNullable(childKind))
                .operation(operation)
                .build());
    }
}
