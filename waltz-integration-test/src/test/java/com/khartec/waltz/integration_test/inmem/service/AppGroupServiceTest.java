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

package com.khartec.waltz.integration_test.inmem.service;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.app_group.AppGroupSubscription;
import org.finos.waltz.model.app_group.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkUserId;
import static org.junit.Assert.*;

public class AppGroupServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppGroupService appGroupSvc;


    @Test
    public void getGroupDetailByIdReturnsNullIfNotFound() {
        AppGroupDetail grp = appGroupSvc.getGroupDetailById(-1);
        assertNull("expect null if group does not exist", grp);
    }


    @Test
    public void checkingDefaultsInNewlyCreatedGroup() {
        String userId = mkUserId("agtest");
        Long gid = appGroupSvc.createNewGroup(userId);
        assertNotNull("Creating a new group gives it an id", gid);

        AppGroupDetail grp = appGroupSvc.getGroupDetailById(gid);
        assertTrue("Newly created group has no applications", grp.applications().isEmpty());
        assertTrue("Newly created group has no ou's", grp.organisationalUnits().isEmpty());
        assertEquals("Groups are private by default", AppGroupKind.PRIVATE, grp.appGroup().appGroupKind());
        assertEquals("Newly created group has one member", 1, grp.members().size());
        AppGroupMember expectedMember = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(userId)
                .role(AppGroupMemberRole.OWNER)
                .build();
        assertEquals("Newly created group has owner as a member", ListUtilities.asList(expectedMember), grp.members());
    }


    @Test
    public void canUpdateGroupInfo() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        Long gid = appGroupSvc.createNewGroup(userId);

        AppGroupDetail grp = appGroupSvc.getGroupDetailById(gid);
        AppGroup newInfo = ImmutableAppGroup
                .copyOf(grp.appGroup())
                .withName("new group name")
                .withDescription("new group desc")
                .withAppGroupKind(AppGroupKind.PUBLIC)
                .withExternalId("agExtId");

        AppGroupDetail updatedGrp = appGroupSvc.updateOverview(userId, newInfo);

        assertEquals("Name can be updated", "new group name", updatedGrp.appGroup().name());
        assertEquals("Description can be updated", "new group desc", updatedGrp.appGroup().description());
        assertEquals("Group kind can be updated", AppGroupKind.PUBLIC, updatedGrp.appGroup().appGroupKind());
        assertEquals("External id can be updated", Optional.of("agExtId"), updatedGrp.appGroup().externalId());
    }


    @Test
    public void onlyOwnersCanUpdate() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        String delegateUserId = mkUserId("agdelegate");

        Long gid = appGroupSvc.createNewGroup(userId);

        AppGroupDetail grp = appGroupSvc.getGroupDetailById(gid);
        AppGroup newInfo = ImmutableAppGroup
                .copyOf(grp.appGroup())
                .withName("new group name");

        assertThrows(
                "cannot update if not an owner",
                InsufficientPrivelegeException.class,
                () -> appGroupSvc.updateOverview(delegateUserId, newInfo));

        assertThrows(
                "non-owner cannot update owner list",
                InsufficientPrivelegeException.class,
                () -> appGroupSvc.addOwner(delegateUserId, gid, mkUserId("wibble")));

        appGroupSvc.addOwner(userId, gid, delegateUserId);
        appGroupSvc.updateOverview(delegateUserId, newInfo);

        AppGroupDetail updatedGrpDetails = appGroupSvc.getGroupDetailById(gid);

        AppGroupMember delegateMember = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(delegateUserId)
                .role(AppGroupMemberRole.OWNER)
                .build();

        assertTrue("delegate has been added", updatedGrpDetails.members().contains(delegateMember));
        assertEquals("name has been updated", "new group name", updatedGrpDetails.appGroup().name());
    }


    @Test
    public void removingAnOwnerDowngradesThemToViewer() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        Long gid = appGroupSvc.createNewGroup(userId);
        assertTrue("cannot remove owner", appGroupSvc.removeOwner(userId, gid, userId));

        AppGroupMember viewer = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(userId)
                .role(AppGroupMemberRole.VIEWER)
                .build();

        assertEquals("still has self as viewer", asSet(viewer), appGroupSvc.getMembers(gid));
    }


    @Test
    public void canFindAllSubscriptionsForUser() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        Long gid1 = appGroupSvc.createNewGroup(userId);
        Long gid2 = appGroupSvc.createNewGroup(userId);
        Long gid3 = appGroupSvc.createNewGroup(userId);

        appGroupSvc.removeOwner(userId, gid3, userId);  // downgrade to viewer

        Set<AppGroupSubscription> subs = appGroupSvc.findGroupSubscriptionsForUser(userId);
        assertEquals("Expected 3 subscriptions for user", 3, subs.size());
        assertEquals(asSet(gid1, gid2, gid3), SetUtilities.map(subs, d -> d.appGroup().id().get()));
    }
}