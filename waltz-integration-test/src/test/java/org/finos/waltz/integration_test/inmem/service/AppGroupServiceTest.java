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

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.app_group.*;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.app_group.AppGroupSubscription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkUserId;
import static org.junit.jupiter.api.Assertions.*;

public class AppGroupServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppGroupService appGroupSvc;


    @Test
    public void getGroupDetailByIdReturnsNullIfNotFound() {
        AppGroupDetail grp = appGroupSvc.getGroupDetailById(-1);
        assertNull(grp, "expect null if group does not exist");
    }


    @Test
    public void checkingDefaultsInNewlyCreatedGroup() {
        String userId = mkUserId("agtest");
        Long gid = appGroupSvc.createNewGroup(userId);
        assertNotNull(gid, "Creating a new group gives it an id");

        AppGroupDetail grp = appGroupSvc.getGroupDetailById(gid);
        assertTrue(grp.applications().isEmpty(), "Newly created group has no applications");
        assertTrue(grp.organisationalUnits().isEmpty(), "Newly created group has no ou's");
        assertEquals(AppGroupKind.PRIVATE, grp.appGroup().appGroupKind(), "Groups are private by default");
        assertEquals(1, grp.members().size(),
                "Newly created group has one member");
        AppGroupMember expectedMember = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(userId)
                .role(AppGroupMemberRole.OWNER)
                .build();
        assertEquals(ListUtilities.asList(expectedMember), grp.members(),
                "Newly created group has owner as a member");
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

        assertEquals("new group name", updatedGrp.appGroup().name(), "Name can be updated");
        assertEquals("new group desc", updatedGrp.appGroup().description(), "Description can be updated");
        assertEquals(AppGroupKind.PUBLIC, updatedGrp.appGroup().appGroupKind(),
                "Group kind can be updated");
        assertEquals(Optional.of("agExtId"), updatedGrp.appGroup().externalId(),
                "External id can be updated");
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
                InsufficientPrivelegeException.class,
                () -> appGroupSvc.updateOverview(delegateUserId, newInfo),
                "cannot update if not an owner");

        assertThrows(
                InsufficientPrivelegeException.class,
                () -> appGroupSvc.addOwner(delegateUserId, gid, mkUserId("wibble")),
                "non-owner cannot update owner list");

        appGroupSvc.addOwner(userId, gid, delegateUserId);
        appGroupSvc.updateOverview(delegateUserId, newInfo);

        AppGroupDetail updatedGrpDetails = appGroupSvc.getGroupDetailById(gid);

        AppGroupMember delegateMember = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(delegateUserId)
                .role(AppGroupMemberRole.OWNER)
                .build();

        assertTrue(updatedGrpDetails.members().contains(delegateMember),
                "delegate has been added");
        assertEquals("new group name", updatedGrpDetails.appGroup().name(),
                "name has been updated");
    }


    @Test
    public void removingAnOwnerDowngradesThemToViewer() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        Long gid = appGroupSvc.createNewGroup(userId);
        assertTrue(appGroupSvc.removeOwner(userId, gid, userId), "cannot remove owner");

        AppGroupMember viewer = ImmutableAppGroupMember
                .builder()
                .groupId(gid)
                .userId(userId)
                .role(AppGroupMemberRole.VIEWER)
                .build();

        assertEquals(asSet(viewer), appGroupSvc.getMembers(gid), "still has self as viewer");
    }


    @Test
    public void canFindAllSubscriptionsForUser() throws InsufficientPrivelegeException {
        String userId = mkUserId("agtest");
        Long gid1 = appGroupSvc.createNewGroup(userId);
        Long gid2 = appGroupSvc.createNewGroup(userId);
        Long gid3 = appGroupSvc.createNewGroup(userId);

        appGroupSvc.removeOwner(userId, gid3, userId);  // downgrade to viewer

        Set<AppGroupSubscription> subs = appGroupSvc.findGroupSubscriptionsForUser(userId);
        assertEquals(3, subs.size(), "Expected 3 subscriptions for user");
        assertEquals(asSet(gid1, gid2, gid3), SetUtilities.map(subs, d -> d.appGroup().id().get()));
    }
}