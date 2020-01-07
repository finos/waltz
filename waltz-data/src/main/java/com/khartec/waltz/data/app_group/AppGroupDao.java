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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

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
        SelectConditionStep<Record1<Long>> groupIds = getPrivateGroupIdByOwner(userId);

        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds)
                    .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name())))
                    .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findRelatedByApplicationId(long appId, String username) {

        SelectConditionStep<Record1<Long>> groupsFromAppGroupEntry = dsl
                .select(APPLICATION_GROUP_ENTRY.GROUP_ID)
                .from(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(appId));

        SelectConditionStep<Record1<Long>> orgUnitIds = dsl
                .select(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .innerJoin(ORGANISATIONAL_UNIT).on(ENTITY_HIERARCHY.ID.eq(ORGANISATIONAL_UNIT.ID))
                .innerJoin(APPLICATION).on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(APPLICATION.ID.eq(appId));

        SelectConditionStep<Record1<Long>> groupsFromAppGroupOUEntry = dsl
                .select(APPLICATION_GROUP_OU_ENTRY.GROUP_ID)
                .from(APPLICATION_GROUP_OU_ENTRY)
                .where(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID.in(orgUnitIds));

        SelectWhereStep<Record1<Long>> appGroups = DSL
                .selectFrom(groupsFromAppGroupEntry.union(groupsFromAppGroupOUEntry).asTable());

        return dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(appGroups))
                .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                        .or(APPLICATION_GROUP.ID.in(getPrivateGroupIdByOwner(username))))
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findRelatedByEntityReference(EntityReference ref, String username) {

        Condition joinOnA = APPLICATION_GROUP.ID.eq(ENTITY_RELATIONSHIP.ID_A)
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APP_GROUP.name()));
        Condition joinOnB = APPLICATION_GROUP.ID.eq(ENTITY_RELATIONSHIP.ID_B)
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APP_GROUP.name()));

        Condition aMatchesEntity = ENTITY_RELATIONSHIP.ID_A.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()));
        Condition bMatchesEntity = ENTITY_RELATIONSHIP.ID_B.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()));

        SelectConditionStep<Record1<Long>> qry = dsl
                .selectDistinct(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .join(ENTITY_RELATIONSHIP)
                .on(joinOnA.or(joinOnB))
                .where((aMatchesEntity.or(bMatchesEntity)))
                .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                        .or(APPLICATION_GROUP.ID.in(getPrivateGroupIdByOwner(username))))
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


    public List<AppGroup> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition searchCondition = terms.stream()
                .map(term -> APPLICATION_GROUP.NAME.like("%" + term + "%"))
                .collect(Collectors.reducing(
                        DSL.trueCondition(),
                        (acc, frag) -> acc.and(frag)));

        Select<Record> publicGroups = dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                    .and(searchCondition))
                    .and(notRemoved);

        Select<Record1<Long>> userGroupIds = getPrivateGroupIdByOwner(options.userId());

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


    private SelectConditionStep<Record1<Long>> getPrivateGroupIdByOwner(String userId) {
        return DSL.select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId)
                        .and(APPLICATION_GROUP_MEMBER.ROLE.eq(AppGroupMemberRole.OWNER.name())));
    }
}
