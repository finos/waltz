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

package com.khartec.waltz.service.app_group;

import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.app_group.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.lang.String.format;

@Service
public class FavouritesService {

    private final AppGroupService appGroupService;
    private final AppGroupMemberDao appGroupMemberDao;
    private final AppGroupEntryDao appGroupEntryDao;
    private final AppGroupDao appGroupDao;
    private final ChangeLogService changeLogService;

    @Autowired
    public FavouritesService(AppGroupService appGroupService,
                             AppGroupMemberDao appGroupMemberDao,
                             AppGroupEntryDao appGroupEntryDao,
                             AppGroupDao appGroupDao,
                             ChangeLogService changeLogService) {
        checkNotNull(appGroupService, "appGroupService cannot be null");
        checkNotNull(appGroupMemberDao, "appGroupMemberDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(appGroupDao, "appGroupDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appGroupService = appGroupService;
        this.appGroupMemberDao = appGroupMemberDao;
        this.appGroupDao = appGroupDao;
        this.changeLogService = changeLogService;
        this.appGroupEntryDao = appGroupEntryDao;
    }


    public AppGroup getFavouritesGroup(String username){
        return appGroupDao.findFavouritesGroupByOwner(username);
    }


    public Collection<AppGroupEntry> getFavouriteGroupEntries(String username) {

        AppGroup favouritesGroup = appGroupDao.findFavouritesGroupByOwner(username);

        if (favouritesGroup != null) {
            return appGroupEntryDao.getEntriesForGroup(favouritesGroup.id().get());
        } else {
            return Collections.emptyList();
        }
    }


    public Collection<AppGroupEntry> addApplication(String username, long applicationId) throws InsufficientPrivelegeException {

        AppGroup favouritesGroup = appGroupDao.findFavouritesGroupByOwner(username);

        Long groupId;

        if (favouritesGroup == null) {
            groupId = createFavouriteAppGroupForUser(username);
        } else {
            groupId = favouritesGroup.id().get();
        }

        return appGroupService.addApplication(username, groupId, applicationId);
    }


    private Long createFavouriteAppGroupForUser(String username) {

        ImmutableAppGroup favouritesGroup = ImmutableAppGroup.builder()
                .name(format("Favourite apps for: %s", username))
                .externalId("FAVOURITE")
                .description("Favourite applications for group owner")
                .appGroupKind(AppGroupKind.PRIVATE)
                .isFavouriteGroup(true)
                .isRemoved(false)
                .build();

        Long groupId = appGroupDao.insert(favouritesGroup);
        appGroupMemberDao.register(groupId, username, AppGroupMemberRole.OWNER);

        audit(groupId, username, "Created group", null, Operation.ADD);

        return groupId;
    }


    public Collection<AppGroupEntry> removeApplication(String username, long applicationId) throws InsufficientPrivelegeException {
        AppGroup favouritesGroup = appGroupDao.findFavouritesGroupByOwner(username);
        return appGroupService.removeApplication(username, favouritesGroup.id().get(), applicationId);
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
