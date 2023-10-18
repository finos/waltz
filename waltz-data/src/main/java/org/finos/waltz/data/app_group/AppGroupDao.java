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

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.app_group.AppGroup;
import org.finos.waltz.model.app_group.AppGroupKind;
import org.finos.waltz.model.app_group.AppGroupMemberRole;
import org.finos.waltz.model.app_group.ImmutableAppGroup;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.schema.tables.records.ApplicationGroupRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.data.JooqUtilities.mkBasicTermSearch;
import static org.finos.waltz.data.SearchUtilities.mkTerms;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.APPLICATION_GROUP_ENTRY;
import static org.finos.waltz.schema.Tables.APPLICATION_GROUP_OU_ENTRY;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static org.finos.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;
import static org.finos.waltz.schema.tables.EntityAlias.ENTITY_ALIAS;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class AppGroupDao implements SearchDao<AppGroup> {


    private static final RecordMapper<? super Record, AppGroup> TO_DOMAIN = r -> {
        ApplicationGroupRecord record = r.into(APPLICATION_GROUP);
        return ImmutableAppGroup.builder()
                .name(record.getName())
                .description(mkSafe(record.getDescription()))
                .id(record.getId())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .appGroupKind(AppGroupKind.valueOf(record.getKind()))
                .isRemoved(record.getIsRemoved())
                .isFavouriteGroup(record.getIsFavouriteGroup())
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
        SelectConditionStep<Record1<Long>> groupIds = findGroupsOwnedByUser(userId);

        return dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds)
                    .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name())))
                    .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    public AppGroup getFavouritesGroupForOwner(String userId) {
        SelectConditionStep<Record1<Long>> groupIds = findGroupsOwnedByUser(userId);

        return dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds)
                        .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name())
                                .and(notRemoved)
                                .and(APPLICATION_GROUP.IS_FAVOURITE_GROUP.isTrue())))
                .fetchOne(TO_DOMAIN);
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

        SelectOrderByStep<Record1<Long>> appGroups = groupsFromAppGroupEntry
                .union(groupsFromAppGroupOUEntry);

        return dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(appGroups))
                .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                        .or(APPLICATION_GROUP.ID.in(findGroupsOwnedByUser(username))))
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
                        .or(APPLICATION_GROUP.ID.in(findGroupsOwnedByUser(username))))
                .and(notRemoved);

        return dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(qry))
                .fetch(TO_DOMAIN);
    }


    public List<AppGroup> findPublicGroups() {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name()))
                .and(APPLICATION_GROUP.IS_FAVOURITE_GROUP.isFalse())
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }


    @Override
    public List<AppGroup> search(EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition nameCondition = mkBasicTermSearch(APPLICATION_GROUP.NAME, terms);
        Condition aliasCondition = ENTITY_ALIAS.KIND.eq(EntityKind.APP_GROUP.name())
                .and(JooqUtilities.mkBasicTermSearch(ENTITY_ALIAS.ALIAS, terms));

        Condition publicGroupCondition = APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name())
                .and(notRemoved);

        Select<Record1<Long>> userGroupIds = findGroupsOwnedByUser(options.userId());

        Condition privateGroupCondition = APPLICATION_GROUP.ID.in(userGroupIds)
                .and(APPLICATION_GROUP.KIND.eq(AppGroupKind.PRIVATE.name()))
                .and(notRemoved);

        Select<Record> publicGroups = dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(publicGroupCondition
                        .and(nameCondition));

        Select<Record> privateGroups = dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(privateGroupCondition
                    .and(nameCondition));

        SelectConditionStep<Record> publicGroupsViaAlias = dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(APPLICATION_GROUP.ID))
                .where(publicGroupCondition)
                .and(aliasCondition);

        SelectConditionStep<Record> privateGroupsViaAlias = dsl
                .select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .innerJoin(ENTITY_ALIAS)
                .on(ENTITY_ALIAS.ID.eq(APPLICATION_GROUP.ID))
                .where(privateGroupCondition)
                .and(aliasCondition);

        Select<Record> searchQry = privateGroups
                .unionAll(publicGroups)
                .unionAll(privateGroupsViaAlias)
                .unionAll(publicGroupsViaAlias);

        return searchQry
                .fetch(TO_DOMAIN)
                .stream()
                .limit(options.limit())
                .collect(Collectors.toList());

    }


    public int update(AppGroup appGroup) {
        return dsl
                .update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.appGroupKind().name())
                .set(APPLICATION_GROUP.EXTERNAL_ID, appGroup.externalId().orElse(null))
                .where(APPLICATION_GROUP.ID.eq(appGroup.id().get()))
                .execute();
    }


    public Long insert(AppGroup appGroup) {
        return dsl
                .insertInto(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.DESCRIPTION, appGroup.description())
                .set(APPLICATION_GROUP.NAME, appGroup.name())
                .set(APPLICATION_GROUP.KIND, appGroup.appGroupKind().name())
                .set(APPLICATION_GROUP.EXTERNAL_ID, appGroup.externalId().orElse(null))
                .set(APPLICATION_GROUP.IS_REMOVED, appGroup.isRemoved())
                .set(APPLICATION_GROUP.IS_FAVOURITE_GROUP, appGroup.isFavouriteGroup())
                .returning(APPLICATION_GROUP.ID)
                .fetchOne()
                .getId();
    }


    public int deleteGroup(long groupId) {
        return dsl.update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.IS_REMOVED, true)
                .where(APPLICATION_GROUP.ID.eq(groupId))
                .and(APPLICATION_GROUP.IS_FAVOURITE_GROUP.isFalse())
                .execute();

    }


    /**
     * Adds and removes app group entries in bulk.
     * @param additions  [tuple2{groupId, ref}]
     * @param removals  [tuple2{groupId, ref}]
     * @param userId  user id to set as the createdBy user
     * @return number of records added + removed
     */
    public int processAdditionsAndRemovals(Set<Tuple2<Long, EntityReference>> additions,
                                            Set<Tuple2<Long, EntityReference>> removals,
                                            String userId) {
        return AppGroupHelper.processAdditionsAndRemovals(
                dsl,
                additions,
                removals,
                userId);
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
                .map(TO_DOMAIN::map)
                .collect(Collectors.toSet());
    }


    // helpers

    private SelectConditionStep<Record1<Long>> findGroupsOwnedByUser(String userId) {
        return DSL
                .select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .innerJoin(APPLICATION_GROUP)
                .on(APPLICATION_GROUP_MEMBER.GROUP_ID.eq(APPLICATION_GROUP.ID))
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId)
                        .and(APPLICATION_GROUP_MEMBER.ROLE.eq(AppGroupMemberRole.OWNER.name())));
    }

    private List<AppGroup> findAppGroupsBySelectorId(SelectConditionStep<Record1<Long>> groupIds) {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds))
                .and(notRemoved)
                .fetch(TO_DOMAIN);
    }

}
