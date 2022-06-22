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

package org.finos.waltz.data.assessment_rating;


import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.*;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.AssessmentDefinition.ASSESSMENT_DEFINITION;
import static org.finos.waltz.schema.tables.AssessmentRating.ASSESSMENT_RATING;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class AssessmentRatingDao {

    private static final org.finos.waltz.schema.tables.AssessmentRating ar = ASSESSMENT_RATING;
    private static final AssessmentDefinition ad = ASSESSMENT_DEFINITION;
    private static final Person p = PERSON;
    private static final PermissionGroupInvolvement pgi = PERMISSION_GROUP_INVOLVEMENT;
    private static final Involvement inv = INVOLVEMENT;
    private static final InvolvementGroupEntry ige = INVOLVEMENT_GROUP_ENTRY;
    private static final UserRole ur = USER_ROLE;
    private static final RatingSchemeItem rsi = RATING_SCHEME_ITEM;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ar.ENTITY_ID,
            ar.ENTITY_KIND,
            newArrayList(EntityKind.values())).as("entity_name");

    private static final Field<String> ENTITY_LIFECYCLE_FIELD = InlineSelectFieldFactory.mkEntityLifecycleField(
            ar.ENTITY_ID,
            ar.ENTITY_KIND,
            newArrayList(EntityKind.values())).as("entity_lifecycle_status");

    private static final Field<String> ENTITY_EXTID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            ar.ENTITY_ID,
            ar.ENTITY_KIND,
            newArrayList(EntityKind.values())).as("entity_external_id");

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER = r -> {
        AssessmentRatingRecord record = r.into(ar);
        return ImmutableAssessmentRating.builder()
                .entityReference(mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .assessmentDefinitionId(record.getAssessmentDefinitionId())
                .ratingId(record.getRatingId())
                .comment(mkSafe(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .isReadOnly(record.getIsReadonly())
                .build();
    };

    private static final RecordUnmapper<AssessmentRating, AssessmentRatingRecord> DOMAIN_TO_RECORD_MAPPER = r -> {
        AssessmentRatingRecord record = new AssessmentRatingRecord();
        record.setAssessmentDefinitionId(r.assessmentDefinitionId());
        record.setEntityKind(r.entityReference().kind().name());
        record.setEntityId(r.entityReference().id());
        record.setRatingId(r.ratingId());
        record.setDescription(r.comment());
        record.setLastUpdatedAt(Timestamp.valueOf(r.lastUpdatedAt()));
        record.setLastUpdatedBy(r.lastUpdatedBy());
        record.setProvenance(r.provenance());
        record.setIsReadonly(r.isReadOnly());
        return record;
    };

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER_WITH_ENTITY_DETAILS  = r -> {
        AssessmentRatingRecord record = r.into(ar);
        return ImmutableAssessmentRating.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .name(ofNullable(r.getValue(ENTITY_NAME_FIELD)))
                        .entityLifecycleStatus(ofNullable(r.getValue(ENTITY_LIFECYCLE_FIELD))
                                .map(EntityLifecycleStatus::valueOf)
                                .orElse(EntityLifecycleStatus.ACTIVE))
                        .externalId(ofNullable(r.getValue(ENTITY_EXTID_FIELD)))
                        .build())
                .assessmentDefinitionId(record.getAssessmentDefinitionId())
                .ratingId(record.getRatingId())
                .comment(mkSafe(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .isReadOnly(record.getIsReadonly())
                .build();
    };


    private final Function<SaveAssessmentRatingCommand, AssessmentRatingRecord> COMMAND_TO_RECORD_MAPPER = command -> {
        AssessmentRatingRecord record = new AssessmentRatingRecord();
        record.setEntityId(command.entityReference().id());
        record.setEntityKind(command.entityReference().kind().name());
        record.setAssessmentDefinitionId(command.assessmentDefinitionId());
        record.setRatingId(command.ratingId());
        record.setDescription(command.comment());
        record.setLastUpdatedAt(Timestamp.valueOf(command.lastUpdatedAt()));
        record.setLastUpdatedBy(command.lastUpdatedBy());
        record.setProvenance(command.provenance());
        record.setIsReadonly(command.isReadOnly()); 
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public AssessmentRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl
                .select(ar.fields())
                .from(ar)
                .where(ar.ENTITY_KIND.eq(ref.kind().name()))
                .and(ar.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AssessmentRating> findByEntityKind(EntityKind kind) {
        return dsl
                .select(ar.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .select(ENTITY_EXTID_FIELD)
                .from(ar)
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID))
                .where(ar.ENTITY_KIND.eq(kind.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AssessmentRating> findByDefinitionId(long definitionId) {
        return dsl
                .select(ar.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .select(ENTITY_EXTID_FIELD)
                .from(ar)
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID))
                .and(ad.ID.eq(definitionId))
                .fetch(TO_DOMAIN_MAPPER_WITH_ENTITY_DETAILS);
    }


    public List<AssessmentRating> findByGenericSelector(GenericSelector genericSelector) {
        return dsl
                .select(ar.fields())
                .from(ar)
                .innerJoin(ad)
                .on(ar.ASSESSMENT_DEFINITION_ID.eq(ad.ID)
                        .and(ad.ENTITY_KIND.eq(genericSelector.kind().name())))
                .where(ar.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(ar.ENTITY_ID.in(genericSelector.selector()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean store(SaveAssessmentRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        AssessmentRatingRecord record = COMMAND_TO_RECORD_MAPPER.apply(command);
        EntityReference ref = command.entityReference();
        boolean isUpdate = dsl.fetchExists(dsl
                .select(ar.fields())
                .from(ar)
                .where(ar.ENTITY_KIND.eq(ref.kind().name()))
                .and(ar.ENTITY_ID.eq(ref.id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(command.assessmentDefinitionId())));

        return isUpdate
                ? dsl.executeUpdate(record) == 1
                : dsl.executeInsert(record) == 1;
    }


    public boolean remove(RemoveAssessmentRatingCommand rating) {
        return dsl.deleteFrom(ar)
                .where(ar.ENTITY_KIND.eq(rating.entityReference().kind().name()))
                .and(ar.ENTITY_ID.eq(rating.entityReference().id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(rating.assessmentDefinitionId()))
                .execute() == 1;
    }

    public int add(Set<AssessmentRating> assessmentRatings) {
        Set<AssessmentRatingRecord> recordsToStore = mkAssessmentRatingRecords(assessmentRatings);
         return dsl.batchInsert(recordsToStore).execute().length;

    }

    public int update(Set<AssessmentRating> assessmentRatings) {
            Set<AssessmentRatingRecord> recordsToUpdate = mkAssessmentRatingRecords(assessmentRatings);
            return dsl.batchUpdate(recordsToUpdate).execute().length;
    }

    public int remove(Set<AssessmentRating> assessmentRatings) {
        Set<AssessmentRatingRecord> ratingsToRemove = mkAssessmentRatingRecords(assessmentRatings);
        return dsl.batchDelete(ratingsToRemove).execute().length;
    }


    private Set<AssessmentRatingRecord> mkAssessmentRatingRecords(Set<AssessmentRating> assessmentRatings) {
        return assessmentRatings
                .stream()
                .map(DOMAIN_TO_RECORD_MAPPER::unmap)
                .collect(toSet());
    }


    public Set<Tuple2<Long, Set<ImmutableRatingEntityList>>> findGroupedByDefinitionAndOutcome(Condition entityCondition) {
        Result<Record> data = dsl
                .select(ar.ASSESSMENT_DEFINITION_ID,
                        rsi.NAME,
                        ar.ENTITY_ID,
                        ar.ENTITY_KIND)
                .select(ENTITY_NAME_FIELD)
                .from(ar)
                .innerJoin(rsi).on(ar.RATING_ID.eq(rsi.ID))
                .where(dsl.renderInlined(entityCondition))
                .fetch();

        Map<Long, Collection<Tuple2<String, EntityReference>>> groupedByDef = groupBy(data,
                r -> r.get(ar.ASSESSMENT_DEFINITION_ID),
                r -> tuple(r.get(rsi.NAME), mkRef(EntityKind.valueOf(r.get(ar.ENTITY_KIND)), r.get(ar.ENTITY_ID), r.get(ENTITY_NAME_FIELD))));

        return groupedByDef
                .entrySet()
                .stream()
                .map(es -> {
                    Map<String, Collection<EntityReference>> entitiesByOutcome = groupBy(es.getValue(), t -> t.v1, t -> t.v2);

                    return tuple(
                            es.getKey(),
                            map(entitiesByOutcome.entrySet(),
                                    entrySet -> ImmutableRatingEntityList
                                            .builder()
                                            .rating(entrySet.getKey())
                                            .entityReferences(entrySet.getValue())
                                            .build()));
                })
                .collect(toSet());
    }


    public Set<Operation> findRatingPermissions(EntityReference entityReference,
                                                long assessmentDefinitionId,
                                                String username) {

        Field<Boolean> userHasDefRoleField = DSL
                .field(ad.PERMITTED_ROLE.isNull()
                    .or(ur.ROLE.isNotNull()))
                .as("user_has_def_role");

        Field<Boolean> userHasOperationField = DSL
                .field(inv.EMPLOYEE_ID.isNotNull())
                .as("user_has_operation");

        Field<Boolean> ratingIsReadOnly = DSL
                .coalesce(ar.IS_READONLY, false)
                .as("rating_is_readonly");

        Condition ratingViaDef = ar.ASSESSMENT_DEFINITION_ID.eq(ad.ID)
                .and(ar.ENTITY_ID.eq(entityReference.id()))
                .and(ar.ENTITY_KIND.eq(ad.ENTITY_KIND));

        Condition permissionViaDef = pgi.QUALIFIER_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name())
                .and(pgi.QUALIFIER_ID.eq(ad.ID))
                .and(pgi.PARENT_KIND.eq(ad.ENTITY_KIND))
                .and(pgi.SUBJECT_KIND.eq(EntityKind.ASSESSMENT_RATING.name()));

        Condition involvementGroupViaPermission = ige.INVOLVEMENT_GROUP_ID.eq(pgi.INVOLVEMENT_GROUP_ID);

        Condition involvementViaInvGroupAndPerson = inv.KIND_ID.eq(ige.INVOLVEMENT_KIND_ID)
                .and(inv.EMPLOYEE_ID.eq(p.EMPLOYEE_ID))
                .and(inv.ENTITY_ID.eq(entityReference.id()))
                .and(inv.ENTITY_KIND.eq(entityReference.kind().name()));

        Condition userRoleViaPersonAndDef = ur.USER_NAME.eq(p.EMAIL)
                .and(ur.ROLE.eq(ad.PERMITTED_ROLE));

        SelectConditionStep<Record5<Boolean, Boolean, Boolean, String, Boolean>> qry = dsl
                .select(ad.IS_READONLY,
                        ratingIsReadOnly,
                        userHasDefRoleField,
                        pgi.OPERATION,
                        userHasOperationField)
                .from(ad)
                .innerJoin(p).on(p.EMAIL.eq(username))
                .leftJoin(ur).on(userRoleViaPersonAndDef)
                .leftJoin(ar).on(ratingViaDef)
                .leftJoin(pgi).on(permissionViaDef)
                .leftJoin(ige).on(involvementGroupViaPermission)
                .leftJoin(inv).on(involvementViaInvGroupAndPerson)
                .where(ad.ID.eq(assessmentDefinitionId));

        Set<Tuple5<Boolean, Boolean, Boolean, String, Boolean>> result = qry
                .fetchSet(r -> tuple(
                    r.get(ad.IS_READONLY),
                    r.get(ratingIsReadOnly),
                    r.get(userHasDefRoleField),
                    r.get(pgi.OPERATION),
                    r.get(userHasOperationField)));

        // { ad.isReadonly, ar.isReadonly, userHasDefRole }
        Tuple3<Boolean, Boolean, Boolean> basicPerms = maybeFirst(result)
                .map(Tuple5::limit3)
                .orElse(null);

        Set<Operation> explicitOperations = result
                .stream()
                .filter(Tuple5::v5)
                .map(t -> Operation.valueOf(t.v4()))
                .collect(toSet());


        if (basicPerms == null || basicPerms.v1) {
            // no perms found or def is marked as readonly
            return emptySet();
        }

        if (basicPerms.v2) {
            // rating is currently locked
            return explicitOperations.contains(Operation.LOCK)
                    ? asSet(Operation.LOCK)
                    : emptySet();
        }

        if (basicPerms.v3) {
            // user has permissions, therefore ensure they have add/remove/update and perhaps lock (from explicit ops)
            return union(
                    explicitOperations,
                    asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        }

        return explicitOperations;
    }


    public boolean lock(EntityReference entityReference,
                        long defId,
                        String username) {
        return setLock(entityReference, defId, username, true);
    }


    public boolean unlock(EntityReference entityReference,
                        long defId,
                        String username) {
        return setLock(entityReference, defId, username, false);
    }


    private boolean setLock(EntityReference entityReference,
                            long defId,
                            String username,
                            boolean isLocked) {
        return dsl
                .update(ar)
                .set(ar.IS_READONLY, isLocked)
                .set(ar.LAST_UPDATED_BY, username)
                .set(ar.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(ar.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(ar.ENTITY_ID.eq(entityReference.id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(defId))
                .execute() == 1;
    }
}
