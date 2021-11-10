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

package org.finos.waltz.data.app_group;

import org.finos.waltz.schema.tables.records.ApplicationGroupMemberRecord;
import org.finos.waltz.model.app_group.AppGroupMember;
import org.finos.waltz.model.app_group.AppGroupMemberRole;
import org.finos.waltz.model.app_group.ImmutableAppGroupMember;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;

@Repository
public class AppGroupMemberDao {

    private final DSLContext dsl;


    @Autowired
    public AppGroupMemberDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<AppGroupMember> getMembers(long groupId) {
        return getWhere(APPLICATION_GROUP_MEMBER.GROUP_ID.eq(groupId));
    }


    public int register(long groupId, String userId) {
        return register(groupId, userId, AppGroupMemberRole.VIEWER);
    }


    public int register(long groupId, String userId, AppGroupMemberRole role) {
        return dsl.insertInto(APPLICATION_GROUP_MEMBER)
                .set(APPLICATION_GROUP_MEMBER.USER_ID, userId)
                .set(APPLICATION_GROUP_MEMBER.GROUP_ID, groupId)
                .set(APPLICATION_GROUP_MEMBER.ROLE, role.name())
                .onDuplicateKeyUpdate()
                .set(APPLICATION_GROUP_MEMBER.ROLE, role.name())
                .execute();

    }


    public Set<AppGroupMember> getSubscriptions(String userId) {
        return getWhere(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));
    }


    public boolean unregister(long groupId, String userId) {
        return dsl.deleteFrom(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId))
                .and(APPLICATION_GROUP_MEMBER.GROUP_ID.eq(groupId))
                .execute() == 1;
    }


    public boolean canUpdate(long groupId, String userId) {
        return dsl.select(DSL.value(Boolean.TRUE))
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId))
                .and(APPLICATION_GROUP_MEMBER.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_MEMBER.ROLE.eq(AppGroupMemberRole.OWNER.name()))
                .fetchOne() != null;
    }


    private Set<AppGroupMember> getWhere(Condition condition) {
        return dsl.select(APPLICATION_GROUP_MEMBER.fields())
                .from(APPLICATION_GROUP_MEMBER)
                .where(condition)
                .fetchSet(r -> {
                    ApplicationGroupMemberRecord record = r.into(APPLICATION_GROUP_MEMBER);
                    return ImmutableAppGroupMember.builder()
                            .userId(record.getUserId())
                            .groupId(record.getGroupId())
                            .role(AppGroupMemberRole.valueOf(record.getRole()))
                            .build();
                });
    }
}
