package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.app_group.AppGroupMemberDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.app_group.AppGroup;
import org.finos.waltz.model.app_group.AppGroupKind;
import org.finos.waltz.model.app_group.AppGroupMemberRole;
import org.finos.waltz.model.app_group.ImmutableAppGroup;
import org.finos.waltz.service.app_group.AppGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class AppGroupHelper {

    @Autowired
    private AppGroupService appGroupService;

    @Autowired
    private AppGroupMemberDao appGroupMemberDao;

    public Long createAppGroupWithAppRefs(String groupName, Collection<EntityReference> appRefs) throws InsufficientPrivelegeException {
        Collection<Long> appIds = CollectionUtilities.map(appRefs, EntityReference::id);
        return createAppGroupWithAppIds(groupName, appIds);
    }


    public Long createAppGroupWithAppIds(String groupName, Collection<Long> appIds) throws InsufficientPrivelegeException {

        Long gId = appGroupService.createNewGroup("appGroupHelper");

        AppGroup g = ImmutableAppGroup
                .builder()
                .id(gId)
                .name(groupName)
                .appGroupKind(AppGroupKind.PUBLIC)
                .build();

        appGroupService.updateOverview("appGroupHelper", g);

        appGroupService.addApplications("appGroupHelper", gId, appIds, Collections.emptySet());

        return gId;
    }


    public void addOwner(Long groupId, String userId) {
        appGroupMemberDao.register(
                groupId,
                userId,
                AppGroupMemberRole.OWNER);
    }
}
