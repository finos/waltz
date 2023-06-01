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
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.*;
import org.finos.waltz.model.tally.ImmutableTally;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.*;
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

    private static boolean DEFAULT_RATING_READ_ONLY_VALUE = false;

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
                .id(record.getId())
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
        record.setDescription(sanitizeCharacters(r.comment()));
        record.setLastUpdatedAt(Timestamp.valueOf(r.lastUpdatedAt()));
        record.setLastUpdatedBy(r.lastUpdatedBy());
        record.setProvenance(r.provenance());
        record.setIsReadonly(r.isReadOnly());
        return record;
    };

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER_WITH_ENTITY_DETAILS  = r -> {
        AssessmentRatingRecord record = r.into(ar);
        return ImmutableAssessmentRating.builder()
                .id(record.getId())
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
        record.setDescription(sanitizeCharacters(command.comment()));
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


    public AssessmentRating getById(long id) {
        return dsl
                .select(ar.fields())
                .from(ar)
                .where(ar.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<AssessmentRating> findByEntityKind(EntityKind kind, Optional<EntityReference> qualifierReference) {

        Condition qualifierCondition = qualifierReference
                .map(ref -> ad.QUALIFIER_KIND.eq(ref.kind().name()).and(ad.QUALIFIER_ID.eq(ref.id())))
                .orElse(DSL.trueCondition());

        return dsl
                .select(ar.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_FIELD)
                .select(ENTITY_EXTID_FIELD)
                .from(ar)
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID)
                        .and(qualifierCondition))
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


    public int deleteByGenericSelector(GenericSelector genericSelector) {
        return dsl
                .deleteFrom(ar)
                .where(ar.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(ar.ENTITY_ID.in(genericSelector.selector()))
                .execute();
    }


    public boolean store(SaveAssessmentRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        AssessmentRatingRecord record = COMMAND_TO_RECORD_MAPPER.apply(command);
        EntityReference ref = command.entityReference();

        boolean isUpdate = dsl.fetchExists(dsl
                .select(ar.ID)
                .from(ar)
                .where(ar.ENTITY_KIND.eq(ref.kind().name()))
                .and(ar.ENTITY_ID.eq(ref.id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(command.assessmentDefinitionId()))
                .and(ar.RATING_ID.eq(command.ratingId())));

        return isUpdate
                ? dsl.executeUpdate(record) == 1
                : dsl.executeInsert(record) == 1;
    }


    public boolean bulkRemove(RemoveAssessmentRatingCommand rating) {
        return dsl
                .deleteFrom(ar)
                .where(ar.ENTITY_KIND.eq(rating.entityReference().kind().name()))
                .and(ar.ENTITY_ID.eq(rating.entityReference().id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(rating.assessmentDefinitionId())
                        .and(ar.RATING_ID.eq(rating.ratingId())))
                .execute() == 1;
    }

    public int add(Set<AssessmentRating> assessmentRatings) {
        Set<AssessmentRatingRecord> recordsToStore = mkAssessmentRatingRecords(assessmentRatings);
        return dsl.batchInsert(recordsToStore).execute().length;

    }

    public int bulkUpdateSingleValuedAssessments(Set<AssessmentRating> assessmentRatings) {

        Set<UpdateConditionStep<AssessmentRatingRecord>> updateStatements = assessmentRatings
                .stream()
                .map(r -> dsl
                        .update(ar)
                        .set(ar.RATING_ID, r.ratingId())
                        .set(ar.DESCRIPTION, r.comment())
                        .where(ar.ASSESSMENT_DEFINITION_ID.eq(r.assessmentDefinitionId())
                                .and(ar.ENTITY_KIND.eq(r.entityReference().kind().name())
                                        .and(ar.ENTITY_ID.eq(r.entityReference().id())))))
                .collect(toSet());

        return dsl.batch(updateStatements).execute().length;
    }

    public int bulkUpdateMultiValuedAssessments(Set<AssessmentRating> assessmentRatings) {

        Set<UpdateConditionStep<AssessmentRatingRecord>> updateStatements = assessmentRatings
                .stream()
                .map(r -> dsl
                        .update(ar)
                        .set(ar.DESCRIPTION, r.comment())
                        .where(ar.ASSESSMENT_DEFINITION_ID.eq(r.assessmentDefinitionId())
                                .and(ar.ENTITY_KIND.eq(r.entityReference().kind().name())
                                        .and(ar.ENTITY_ID.eq(r.entityReference().id())
                                                .and(ar.RATING_ID.eq(r.ratingId()))))))
                .collect(toSet());

        return dsl.batch(updateStatements).execute().length;
    }

    public int bulkRemove(Set<AssessmentRating> assessmentRatings) {
        Set<DeleteConditionStep<AssessmentRatingRecord>> deleteStatements = map(
                assessmentRatings,
                d -> dsl
                        .deleteFrom(ar)
                        .where(ar.ASSESSMENT_DEFINITION_ID.eq(d.assessmentDefinitionId())
                                .and(ar.ENTITY_KIND.eq(d.entityReference().kind().name())
                                        .and(ar.ENTITY_ID.eq(d.entityReference().id())
                                                .and(ar.RATING_ID.eq(d.ratingId()))))));
        return dsl.batch(deleteStatements).execute().length;
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
                            SetUtilities.map(entitiesByOutcome.entrySet(),
                                    entrySet -> ImmutableRatingEntityList
                                            .builder()
                                            .rating(entrySet.getKey())
                                            .entityReferences(entrySet.getValue())
                                            .build()));
                })
                .collect(toSet());
    }


    public boolean lock(EntityReference entityReference,
                        long defId,
                        long ratingId,
                        String username) {
        return setLock(entityReference, defId, username, ratingId, true);
    }


    public boolean unlock(EntityReference entityReference,
                          long defId,
                          long ratingId,
                          String username) {
        return setLock(entityReference, defId, username, ratingId, false);
    }


    private boolean setLock(EntityReference entityReference,
                            long defId,
                            String username,
                            long ratingId,
                            boolean isLocked) {
        return dsl
                .update(ar)
                .set(ar.IS_READONLY, isLocked)
                .set(ar.LAST_UPDATED_BY, username)
                .set(ar.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(ar.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(ar.ENTITY_ID.eq(entityReference.id()))
                .and(ar.ASSESSMENT_DEFINITION_ID.eq(defId))
                .and(ar.RATING_ID.eq(ratingId))
                .execute() == 1;
    }

    public Set<AssessmentRatingOperations> calculateAmendedRatingOperations(Set<Operation> operationsForEntityAssessment,
                                                                            EntityReference entityReference,
                                                                            long assessmentDefinitionId,
                                                                            String username) {

        Field<Boolean> readOnlyRatingField = DSL.coalesce(ASSESSMENT_RATING.IS_READONLY, DSL.val(DEFAULT_RATING_READ_ONLY_VALUE)).as("rating_read_only");

        SelectConditionStep<Record5<String, String, Boolean, Boolean, Long>> qry = dsl
                .select(USER_ROLE.ROLE,
                        ASSESSMENT_DEFINITION.PERMITTED_ROLE,
                        ASSESSMENT_DEFINITION.IS_READONLY,
                        readOnlyRatingField,
                        ASSESSMENT_RATING.RATING_ID)
                .from(ASSESSMENT_DEFINITION)
                .leftJoin(ASSESSMENT_RATING)
                .on(ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(entityReference.id())
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(entityReference.kind().name())))
                .leftJoin(USER_ROLE)
                .on(USER_ROLE.ROLE.eq(ASSESSMENT_DEFINITION.PERMITTED_ROLE)
                        .and(USER_ROLE.USER_NAME.eq(username)))
                .where(ASSESSMENT_DEFINITION.ID.eq(assessmentDefinitionId));

        Set<Tuple5<Boolean, Boolean, Boolean, Boolean, Long>> rawRecords = qry
                .fetchSet(r -> tuple(
                        notEmpty(r.get(ASSESSMENT_DEFINITION.PERMITTED_ROLE)),
                        notEmpty(r.get(USER_ROLE.ROLE)),
                        r.get(ASSESSMENT_DEFINITION.IS_READONLY),
                        r.get(readOnlyRatingField),
                        r.get(ASSESSMENT_RATING.RATING_ID)));

        Optional<AssessmentRatingOperations> defaultRatingOperations = rawRecords
                .stream()
                .findFirst()
                .map(t -> {
                    Set<Operation> dlftOps = determineOperations(
                            operationsForEntityAssessment,
                            t.v1,
                            t.v2,
                            t.v3,
                            DEFAULT_RATING_READ_ONLY_VALUE);

                    return ImmutableAssessmentRatingOperations.builder()
                            .ratingId(null)
                            .operations(dlftOps)
                            .build();
                });

        Set<AssessmentRatingOperations> permissionRecords = rawRecords
                .stream()
                .map(t -> {

                    Boolean defHasPermittedRole = t.v1;
                    Boolean userHasPermittedRole = t.v2;
                    Boolean defIsReadOnly = t.v3;
                    Boolean ratingIsReadOnly = t.v4;
                    Long ratingId = t.v5;

                    Set<Operation> operations = determineOperations(
                            operationsForEntityAssessment,
                            defHasPermittedRole,
                            userHasPermittedRole,
                            defIsReadOnly,
                            ratingIsReadOnly);

                    return ImmutableAssessmentRatingOperations.builder()
                            .ratingId(ratingId)
                            .operations(operations)
                            .build();
                })
                .collect(toSet());


        return maybeAdd(permissionRecords, defaultRatingOperations);
    }

    private Set<Operation> determineOperations(Set<Operation> operationsForEntityAssessment,
                                               Boolean defHasPermittedRole,
                                               Boolean userHasPermittedRole,
                                               Boolean defIsReadOnly,
                                               Boolean ratingIsReadOnly) {

        if (defIsReadOnly) {
            return emptySet();
        } else if (ratingIsReadOnly) {
            return intersection(operationsForEntityAssessment, asSet(Operation.LOCK));
        } else if (userHasPermittedRole || !defHasPermittedRole) {
            return union(operationsForEntityAssessment, asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE));
        } else {
            return operationsForEntityAssessment;
        }
    }

    public boolean updateComment(long id, String comment, String username) {
        return dsl
                .update(ar)
                .set(ar.DESCRIPTION, comment)
                .set(ar.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(ar.LAST_UPDATED_BY, username)
                .where(ar.ID.eq(id))
                .execute() == 1;
    }


    public boolean updateRating(long assessmentRatingId, UpdateRatingCommand ratingCommand, String username) {
        return dsl
                .update(ar)
                .set(ar.RATING_ID, ratingCommand.newRatingId())
                .set(ar.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(ar.LAST_UPDATED_BY, username)
                .where(ar.ID.eq(assessmentRatingId))
                .execute() == 1;
    }

    public Set<AssessmentRatingSummaryCounts> findRatingSummaryCounts(GenericSelector genericSelector,
                                                                      Set<Long> definitionIds) {

        Condition definitionCondition = isEmpty(definitionIds)
                ? DSL.trueCondition()
                : ar.ASSESSMENT_DEFINITION_ID.in(definitionIds);

        AggregateFunction<Integer> entityCount = DSL.count(ar.ENTITY_ID);
        return dsl
                .select(ar.ASSESSMENT_DEFINITION_ID, ar.RATING_ID, entityCount)
                .from(ar)
                .where(ar.ENTITY_ID.in(genericSelector.selector())
                        .and(ar.ENTITY_KIND.eq(genericSelector.kind().name()))
                        .and(definitionCondition))
                .groupBy(ar.ASSESSMENT_DEFINITION_ID, ar.RATING_ID)
                .fetchGroups(ar.ASSESSMENT_DEFINITION_ID)
                .entrySet()
                .stream()
                .map(e -> {

                    Set<ImmutableTally<Long>> ratingCounts = SetUtilities.map(e.getValue(),
                            r -> ImmutableTally
                                    .<Long>builder()
                                    .id(r.get(ar.RATING_ID))
                                    .count(r.get(entityCount))
                                    .build());

                    return ImmutableAssessmentRatingSummaryCounts
                            .builder()
                            .definitionId(e.getKey())
                            .ratingCounts(ratingCounts)
                            .build();
                })
                .collect(toSet());
    }

    public boolean hasMultiValuedAssessments(long assessmentDefinitionId) {
        AggregateFunction<Integer> ratingCount = DSL.count(ar.RATING_ID);
        return dsl
                .select(ar.ENTITY_ID, ratingCount)
                .from(ar)
                .where(ar.ASSESSMENT_DEFINITION_ID.eq(assessmentDefinitionId))
                .groupBy(ar.ENTITY_ID)
                .having(ratingCount.gt(1))
                .fetch()
                .isNotEmpty();
    }
}
