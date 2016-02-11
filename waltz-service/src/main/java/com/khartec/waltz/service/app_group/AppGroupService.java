package com.khartec.waltz.service.app_group;

import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.model.app_group.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.MapUtilities.indexBy;

@Service
public class AppGroupService {

    private final AppGroupDao appGroupDao;
    private final AppGroupMemberDao appGroupMemberDao;
    private final AppGroupEntryDao appGroupEntryDao;

    @Autowired
    public AppGroupService(AppGroupDao appGroupDao, AppGroupMemberDao appGroupMemberDao, AppGroupEntryDao appGroupEntryDao) {
        this.appGroupDao = appGroupDao;
        this.appGroupMemberDao = appGroupMemberDao;
        this.appGroupEntryDao = appGroupEntryDao;
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
        appGroupMemberDao.subscribe(userId, groupId);
    }

    public void unsubscribe(String userId, long groupId) {
        appGroupMemberDao.subscribe(userId, groupId);
    }
}
