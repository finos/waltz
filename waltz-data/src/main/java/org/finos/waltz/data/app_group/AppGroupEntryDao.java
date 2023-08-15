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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.finos.waltz.model.entity_relationship.RelationshipKind;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.ApplicationGroupEntry;
import org.finos.waltz.schema.tables.ApplicationGroupOuEntry;
import org.finos.waltz.schema.tables.ChangeInitiative;
import org.finos.waltz.schema.tables.EntityRelationship;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.schema.tables.records.ApplicationGroupEntryRecord;
import org.finos.waltz.schema.tables.records.EntityRelationshipRecord;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.Application.APPLICATION;


@Repository
public class AppGroupEntryDao {


    private static final Application app = Tables.APPLICATION;
    private static final ApplicationGroupEntry age = Tables.APPLICATION_GROUP_ENTRY;
    private static final ApplicationGroupOuEntry agoe = Tables.APPLICATION_GROUP_OU_ENTRY;
    private static final ChangeInitiative ci = Tables.CHANGE_INITIATIVE;
    private static final OrganisationalUnit ou = Tables.ORGANISATIONAL_UNIT;
    private static final EntityRelationship er = Tables.ENTITY_RELATIONSHIP;

    private static final RecordMapper<Record, AppGroupEntry> appRefMapper = r ->
            ImmutableAppGroupEntry.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(r.getValue(APPLICATION.ID))
                    .name(r.getValue(APPLICATION.NAME))
                    .provenance(r.getValue(age.PROVENANCE))
                    .isReadOnly(r.getValue(age.IS_READONLY))
                    .build();


    private final DSLContext dsl;


    @Autowired
    public AppGroupEntryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AppGroupEntry> findEntriesForGroup(long groupId) {
        return dsl
                .select(app.ID, app.NAME)
                .select(age.IS_READONLY, age.PROVENANCE)
                .from(app)
                .innerJoin(age).on(age.APPLICATION_ID.eq(app.ID))
                .where(age.GROUP_ID.eq(groupId))
                .and(IS_ACTIVE)
                .fetch(appRefMapper);
    }

    public int addApplication(long groupId, long applicationId) {
        return dsl
                .insertInto(age)
                .set(age.GROUP_ID, groupId)
                .set(age.APPLICATION_ID, applicationId)
                .onDuplicateKeyIgnore()
                .execute();
    }


    public int[] addApplications(long groupId, Collection<Long> applicationIds) {
        Query[] queries = applicationIds
                .stream()
                .map(id -> DSL
                        .insertInto(age)
                        .set(age.GROUP_ID, groupId)
                        .set(age.APPLICATION_ID, id)
                        .onDuplicateKeyIgnore())
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
    }


    public int removeApplication(long groupId, long applicationId) {
        return dsl
                .delete(age)
                .where(age.GROUP_ID.eq(groupId))
                .and(age.APPLICATION_ID.eq(applicationId)
                        .and(age.IS_READONLY.isFalse()))
                .execute();
    }


    public int removeApplications(long groupId, List<Long> applicationIds) {
        return dsl
                .delete(age)
                .where(age.GROUP_ID.eq(groupId))
                .and(age.APPLICATION_ID.in(applicationIds)
                        .and(age.IS_READONLY.isFalse()))
                .execute();
    }

    public void replaceGroupApplicationEntries(Set<Tuple2<Long, Set<AppGroupEntry>>> entriesForGroups) {
        Set<Long> groupIds = map(entriesForGroups, d -> d.v1);

        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();

            tx.deleteFrom(age)
                    .where(age.GROUP_ID.in(groupIds))
                    .execute();

            entriesForGroups
                    .stream()
                    .flatMap(t -> t.v2
                            .stream()
                            .map(r -> {
                                ApplicationGroupEntryRecord record = tx.newRecord(age);
                                record.setGroupId(t.v1);
                                record.setApplicationId(r.id());
                                record.setIsReadonly(r.isReadOnly());
                                record.setProvenance(r.provenance());
                                record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
                                return record;
                            }))
                    .collect(collectingAndThen(toSet(), tx::batchInsert))
                    .execute();
        });
    }

    public void replaceGroupChangeInitiativeEntries(Set<Tuple2<Long, Set<AppGroupEntry>>> entriesForGroups) {
        Set<Long> groupIds = map(entriesForGroups, d -> d.v1);

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();

            tx.deleteFrom(er)
                    .where(er.ID_A.in(groupIds)
                            .and(er.KIND_A.eq(EntityKind.APP_GROUP.name())
                                    .and(er.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))))
                    .execute();

            entriesForGroups
                    .stream()
                    .flatMap(t -> t.v2
                            .stream()
                            .map(r -> {
                                EntityRelationshipRecord record = tx.newRecord(er);
                                record.setIdA(t.v1);
                                record.setKindA(EntityKind.APP_GROUP.name());
                                record.setIdB(r.id());
                                record.setKindB(EntityKind.CHANGE_INITIATIVE.name());
                                record.setProvenance(r.provenance());
                                record.setRelationship(RelationshipKind.RELATES_TO.name());
                                record.setLastUpdatedAt(now);
                                record.setLastUpdatedBy("admin");
                                return record;
                            }))
                    .collect(collectingAndThen(toSet(), tx::batchInsert))
                    .execute();
        });
    }


    public Map<Long, List<EntityReference>> fetchEntitiesForGroups(Set<Long> groupIds) {
        SelectConditionStep<Record4<Long, String, Long, String>> apps = dsl
                .select(age.GROUP_ID,
                        DSL.val(EntityKind.APPLICATION.name()).as("kind"),
                        app.ID,
                        app.NAME)
                .from(age)
                .innerJoin(app).on(app.ID.eq(age.APPLICATION_ID))
                .where(age.GROUP_ID.in(groupIds));

        SelectConditionStep<Record4<Long, String, Long, String>> ous = dsl
                .select(agoe.GROUP_ID,
                        DSL.val(EntityKind.ORG_UNIT.name()).as("kind"),
                        ou.ID,
                        ou.NAME)
                .from(agoe)
                .innerJoin(ou).on(ou.ID.eq(agoe.GROUP_ID))
                .where(agoe.GROUP_ID.in(groupIds));

        SelectConditionStep<Record4<Long, String, Long, String>> cis = dsl
                .select(er.ID_A,
                        DSL.val(EntityKind.CHANGE_INITIATIVE.name()).as("kind"),
                        ci.ID,
                        ci.NAME)
                .from(er)
                .innerJoin(ci).on(ci.ID.eq(er.ID_B))
                .where(er.KIND_A.eq(EntityKind.APP_GROUP.name())
                        .and(er.ID_A.in(groupIds)));

        return apps
                .union(ous)
                .union(cis)
                .fetchGroups(
                        r -> r.get(0, Long.class),
                        r -> mkRef(
                                EntityKind.valueOf(r.get(1, String.class)),
                                r.get(2, Long.class),
                                r.get(3, String.class)));
    }
}
