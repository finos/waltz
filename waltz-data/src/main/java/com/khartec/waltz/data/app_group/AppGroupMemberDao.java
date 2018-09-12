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

package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.app_group.AppGroupMember;
import com.khartec.waltz.model.app_group.AppGroupMemberRole;
import com.khartec.waltz.model.app_group.ImmutableAppGroupMember;
import com.khartec.waltz.schema.tables.records.ApplicationGroupMemberRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;

@Repository
public class AppGroupMemberDao {

    private final DSLContext dsl;


    @Autowired
    public AppGroupMemberDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AppGroupMember> getMembers(long groupId) {
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


    public List<AppGroupMember> getSubscriptions(String userId) {
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


    private List<AppGroupMember> getWhere(Condition condition) {
        return dsl.select(APPLICATION_GROUP_MEMBER.fields())
                .from(APPLICATION_GROUP_MEMBER)
                .where(condition)
                .fetch(r -> {
                    ApplicationGroupMemberRecord record = r.into(APPLICATION_GROUP_MEMBER);
                    return ImmutableAppGroupMember.builder()
                            .userId(record.getUserId())
                            .groupId(record.getGroupId())
                            .role(AppGroupMemberRole.valueOf(record.getRole()))
                            .build();
                });
    }
}
