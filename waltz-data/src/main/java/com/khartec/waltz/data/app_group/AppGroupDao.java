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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupKind;
import com.khartec.waltz.model.app_group.AppGroupMemberRole;
import com.khartec.waltz.model.app_group.ImmutableAppGroup;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.schema.tables.records.ApplicationGroupRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.Tables.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.Tables.ENTITY_RELATIONSHIP;
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
                .externalId(Optional.ofNullable(record.getExternalId()))
                .appGroupKind(AppGroupKind.valueOf(record.getKind()))
                .isRemoved(record.getIsRemoved())
                .build();
    };


    private final DSLContext dsl;

    private final Condition notRemoved = APPLICATION_GROUP.IS_REMOVED.eq(false);


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
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId))
                .and(notRemoved);

        return findAppGroupsBySelectorId(groupIds);
    }


    public List<AppGroup> findPrivateGroupsByOwner(String userId) {
        SelectConditionStep<Record1<Long>> groupIds = DSL.select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId)
                    .and(APPLICATION_GROUP_MEMBER.ROLE.eq(AppGroupMemberRole.OWNER.name())));

        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds)
                    .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name())))
                    .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findRelatedByApplicationAndUser(long appId, String userId) {
        SelectConditionStep<Record1<Long>> groupsByUser = DSL
                .select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));

        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .join(APPLICATION_GROUP_ENTRY).on(APPLICATION_GROUP.ID.eq(APPLICATION_GROUP_ENTRY.GROUP_ID))
                .where(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(appId))
                .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name()).or(APPLICATION_GROUP.ID.in(groupsByUser)))
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findRelatedByEntityReferenceAndUser(EntityReference ref, String userId) {
        SelectConditionStep<Record1<Long>> groupsByUser = DSL
                .select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));

        Condition joinOnA = APPLICATION_GROUP.ID.eq(ENTITY_RELATIONSHIP.ID_A)
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APP_GROUP.name()));
        Condition joinOnB = APPLICATION_GROUP.ID.eq(ENTITY_RELATIONSHIP.ID_B)
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APP_GROUP.name()));

        Condition aMatchesEntity = ENTITY_RELATIONSHIP.ID_A.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()));
        Condition bMatchesEntity = ENTITY_RELATIONSHIP.ID_B.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()));

        Condition isVisibleToUser = APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                .or(APPLICATION_GROUP.ID.in(groupsByUser));

        SelectConditionStep<Record1<Long>> qry = dsl
                .selectDistinct(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .join(ENTITY_RELATIONSHIP)
                .on(joinOnA.or(joinOnB))
                .where((aMatchesEntity.or(bMatchesEntity)).and(isVisibleToUser))
                .and(notRemoved);

        return dsl.selectFrom(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(qry))
                .fetch(TO_DOMAIN);
    }


    private List<AppGroup> findAppGroupsBySelectorId(SelectConditionStep<Record1<Long>> groupIds) {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds))
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findPublicGroups() {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name()))
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> search(String terms, EntitySearchOptions options) {
        checkNotNull(terms, "terms cannot be null");
        checkNotNull(options, "options cannot be null");

        Condition searchCondition = APPLICATION_GROUP.NAME.like("%" + terms + "%");

        Select<Record> publicGroups = dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                    .and(searchCondition))
                    .and(notRemoved);


        Select<Record1<Long>> userGroupIds = DSL.select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(options.userId())
                        .and(APPLICATION_GROUP_MEMBER.ROLE.eq(AppGroupMemberRole.OWNER.name())));

        Select<Record> privateGroups = dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(userGroupIds)
                    .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name()))
                    .and(searchCondition))
                    .and(notRemoved);

        return publicGroups
                .unionAll(privateGroups)
                .fetch(TO_DOMAIN);
    }


    public int update(AppGroup appGroup) {
        return dsl.update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.appGroupKind().name())
                .where(APPLICATION_GROUP.ID.eq(appGroup.id().get()))
                .execute();
    }

    public Long insert(AppGroup appGroup) {
        return dsl.insertInto(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.appGroupKind().name())
                .returning(APPLICATION_GROUP.ID)
                .fetchOne()
                .getId();
    }

    public int deleteGroup(long groupId) {
        return dsl.update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.IS_REMOVED, true)
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
                .and(notRemoved)
                .fetch()
                .stream()
                .map(r -> TO_DOMAIN.map(r))
                .collect(Collectors.toSet());
    }
}
