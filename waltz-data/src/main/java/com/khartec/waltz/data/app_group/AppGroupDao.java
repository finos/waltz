/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupKind;
import com.khartec.waltz.model.app_group.ImmutableAppGroup;
import com.khartec.waltz.schema.tables.records.ApplicationGroupRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;

@Repository
public class AppGroupDao {


    private static final RecordMapper<? super Record, AppGroup> TO_DOMAIN = r -> {
        ApplicationGroupRecord record = r.into(APPLICATION_GROUP);
        return ImmutableAppGroup.builder()
                .name(record.getName())
                .description(mkSafe(record.getDescription()))
                .id(record.getId())
                .kind(AppGroupKind.valueOf(record.getKind()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public AppGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public AppGroup getGroup(long groupId) {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.eq(groupId))
                .fetchOne(TO_DOMAIN);
    }


    public List<AppGroup> findGroupsForUser(String userId) {
        SelectConditionStep<Record1<Long>> groupIds = DSL.select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));

        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds))
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findPublicGroups() {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name()))
                .fetch(TO_DOMAIN);
    }

    public int update(AppGroup appGroup) {
        return dsl.update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.kind().name())
                .where(APPLICATION_GROUP.ID.eq(appGroup.id().get()))
                .execute();
    }

    public Long insert(AppGroup appGroup) {
        return dsl.insertInto(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.kind().name())
                .returning(APPLICATION_GROUP.ID)
                .fetchOne()
                .getId();
    }

    public int deleteGroup(long groupId) {
        return dsl.delete(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.eq(groupId))
                .execute();

    }

    public Set<AppGroup> findByIds(String user, List<Long> ids) {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .innerJoin(APPLICATION_GROUP_MEMBER)
                .on(APPLICATION_GROUP_MEMBER.GROUP_ID.eq(APPLICATION_GROUP.ID))
                .where(APPLICATION_GROUP.ID.in(ids))
                .and(APPLICATION_GROUP_MEMBER.USER_ID.eq(user)
                        .or(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())))
                .fetch()
                .stream()
                .map(r -> TO_DOMAIN.map(r))
                .collect(Collectors.toSet());
    }
}
