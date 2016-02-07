package com.khartec.waltz.service.app_group;

import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupDetail;
import com.khartec.waltz.model.app_group.AppGroupMember;
import com.khartec.waltz.model.app_group.ImmutableAppGroupDetail;
import com.khartec.waltz.model.tally.EntityReferenceTally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<AppGroup> findGroupsForUser(String userId) {
        return appGroupDao.findGroupsForUser(userId);
    }

    public List<AppGroup> findPublicGroups() {
        return appGroupDao.findPublicGroups();
    }
}
