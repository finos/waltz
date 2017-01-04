package com.khartec.waltz.service.app_group;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.app_group.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entiy_relationship.EntityRelationship;
import com.khartec.waltz.model.entiy_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.indexBy;

@Service
public class AppGroupService {

    private final AppGroupDao appGroupDao;
    private final AppGroupMemberDao appGroupMemberDao;
    private final AppGroupEntryDao appGroupEntryDao;
    private final ApplicationDao applicationDao;
    private final EntityRelationshipDao entityRelationshipDao;
    private final ChangeInitiativeDao changeInitiativeDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public AppGroupService(AppGroupDao appGroupDao,
                           AppGroupMemberDao appGroupMemberDao,
                           AppGroupEntryDao appGroupEntryDao,
                           ApplicationDao applicationDao,
                           EntityRelationshipDao entityRelationshipDao,
                           ChangeInitiativeDao changeInitiativeDao,
                           ChangeLogService changeLogService) {
        this.applicationDao = applicationDao;
        checkNotNull(appGroupDao, "appGroupDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appGroupDao = appGroupDao;
        this.appGroupMemberDao = appGroupMemberDao;
        this.appGroupEntryDao = appGroupEntryDao;
        this.entityRelationshipDao = entityRelationshipDao;
        this.changeInitiativeDao = changeInitiativeDao;
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


    public void subscribe(String userId, long groupId) {
        audit(groupId, userId, "Subscribed to group");
        appGroupMemberDao.register(groupId, userId);
    }


    public void unsubscribe(String userId, long groupId) {
        audit(groupId, userId, "Unsubscribed from group");
        appGroupMemberDao.unregister(groupId, userId);
    }


    public List<AppGroupSubscription> deleteGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupDao.deleteGroup(groupId);
        audit(groupId, userId, String.format("Deleted group %d", groupId));
        return findGroupSubscriptionsForUser(userId);
    }


    public List<EntityReference> addApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {

        verifyUserCanUpdateGroup(userId, groupId);

        Application app = applicationDao.getById(applicationId);
        if (app != null) {
            appGroupEntryDao.addApplication(groupId, applicationId);
            audit(groupId, userId, String.format("Added application %s to group", app.name()));
        }

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> removeApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupEntryDao.removeApplication(groupId, applicationId);
        Application app = applicationDao.getById(applicationId);
        audit(groupId, userId, String.format(
                "Added application %s to group",
                app != null
                    ? app.name()
                    : app.id()));
        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public int addOwner(String userId, long groupId, String ownerId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        audit(groupId, userId, String.format("Added owner %s to group %d", ownerId, groupId));
        return appGroupMemberDao.register(groupId, ownerId, AppGroupMemberRole.OWNER);
    }


    public List<AppGroupMember> getMembers(long groupId) {
        return appGroupMemberDao.getMembers(groupId);
    }


    public AppGroupDetail updateOverview(String userId, AppGroup appGroup) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, appGroup.id().get());
        appGroupDao.update(appGroup);
        audit(appGroup.id().get(), userId, "Updated group overview");
        return getGroupDetailById(appGroup.id().get());
    }


    public Long createNewGroup(String userId) {
        long groupId = appGroupDao.insert(ImmutableAppGroup.builder()
                .description("New group created by: " + userId)
                .name("New group created by: "+userId)
                .kind(AppGroupKind.PRIVATE)
                .build());

        appGroupMemberDao.register(groupId, userId, AppGroupMemberRole.OWNER);

        audit(groupId, userId, "Created group");


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

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(groupId, changeInitiativeId);
        entityRelationshipDao.save(entityRelationship);

        audit(groupId, username, String.format("Associated change initiative: %d", changeInitiativeId));
        return getChangeInitiatives(groupId);
    }


    public Collection<ChangeInitiative> removeChangeInitiative(
            String username,
            long groupId,
            long changeInitiativeId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(username, groupId);

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(groupId, changeInitiativeId);
        entityRelationshipDao.remove(entityRelationship);
        audit(groupId, username, String.format("Removed associated change initiative: %d", changeInitiativeId));

        return getChangeInitiatives(groupId);
    }


    private void verifyUserCanUpdateGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        if (!appGroupMemberDao.canUpdate(groupId, userId)) {
            throw new InsufficientPrivelegeException(userId + " cannot update group: " + groupId);
        }
    }


    private EntityRelationship buildChangeInitiativeRelationship(long groupId, long changeInitiativeId) {
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
                .provenance("waltz")
                .build();
    }


    private Collection<ChangeInitiative> getChangeInitiatives(long groupId) {
        return changeInitiativeDao.findForEntityReference(ImmutableEntityReference.builder()
                .id(groupId)
                .kind(EntityKind.APP_GROUP)
                .build());
    }


    private void audit(long groupId, String userId, String message) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .userId(userId)
                .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                .build());
    }

}
