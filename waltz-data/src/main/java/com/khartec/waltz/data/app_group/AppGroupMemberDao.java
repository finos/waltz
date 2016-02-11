/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.app_group.AppGroupMember;
import com.khartec.waltz.model.app_group.AppGroupMemberRole;
import com.khartec.waltz.model.app_group.ImmutableAppGroupMember;
import com.khartec.waltz.schema.tables.records.ApplicationGroupMemberRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
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


    public void subscribe(String userId, long groupId) {
        ApplicationGroupMemberRecord record = new ApplicationGroupMemberRecord(groupId, userId, AppGroupMemberRole.VIEWER.name());
        dsl.executeInsert(record);
    }


    public List<AppGroupMember> getSubscriptions(String userId) {
        return getWhere(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));
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
