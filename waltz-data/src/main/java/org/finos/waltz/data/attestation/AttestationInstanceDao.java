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

package org.finos.waltz.data.attestation;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.Criticality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.ApplicationKind;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.attestation.ApplicationAttestationInstanceSummary;
import org.finos.waltz.model.attestation.ApplicationAttestationSummaryCounts;
import org.finos.waltz.model.attestation.AttestEntityCommand;
import org.finos.waltz.model.attestation.AttestationCount;
import org.finos.waltz.model.attestation.AttestationInstance;
import org.finos.waltz.model.attestation.ImmutableApplicationAttestationInstanceSummary;
import org.finos.waltz.model.attestation.ImmutableApplicationAttestationSummaryCounts;
import org.finos.waltz.model.attestation.ImmutableAttestationCount;
import org.finos.waltz.model.attestation.ImmutableAttestationInstance;
import org.finos.waltz.model.attestation.ImmutableLatestMeasurableAttestationInfo;
import org.finos.waltz.model.attestation.ImmutableSyncRecipientsResponse;
import org.finos.waltz.model.attestation.LatestMeasurableAttestationInfo;
import org.finos.waltz.model.attestation.SyncRecipientsResponse;
import org.finos.waltz.schema.tables.records.AttestationInstanceRecord;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Record5;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Row2;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class AttestationInstanceDao {

    private static final Logger LOG = LoggerFactory.getLogger(AttestationInstanceDao.class);

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ATTESTATION_INSTANCE.PARENT_ENTITY_ID,
            ATTESTATION_INSTANCE.PARENT_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private final DSLContext dsl;

    private static final RecordMapper<Record, AttestationInstance> TO_DOMAIN_MAPPER = r -> {
        AttestationInstanceRecord record = r.into(ATTESTATION_INSTANCE);
        return ImmutableAttestationInstance.builder()
                .id(record.getId())
                .attestationRunId(record.getAttestationRunId())
                .parentEntity(mkRef(
                        EntityKind.valueOf(record.getParentEntityKind()),
                        record.getParentEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .attestedAt(Optional.ofNullable(record.getAttestedAt()).map(Timestamp::toLocalDateTime))
                .attestedBy(Optional.ofNullable(record.getAttestedBy()))
                .attestedEntityKind(EntityKind.valueOf(record.getAttestedEntityKind()))
                .attestedEntityId(r.get(ATTESTATION_RUN.ATTESTED_ENTITY_ID))
                .build();
    };


    @Autowired
    public AttestationInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public AttestationInstance getById (long id) {

        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .where(ATTESTATION_INSTANCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public long create(AttestationInstance attestationInstance) {
        checkNotNull(attestationInstance, "attestationInstance cannot be null");

        AttestationInstanceRecord record = dsl.newRecord(ATTESTATION_INSTANCE);
        record.setAttestationRunId(attestationInstance.attestationRunId());
        record.setParentEntityKind(attestationInstance.parentEntity().kind().name());
        record.setParentEntityId(attestationInstance.parentEntity().id());
        record.setAttestedEntityKind(attestationInstance.attestedEntityKind().name());

        record.store();

        return record.getId();
    }


    public List<AttestationInstance> findByRecipient(String userId, boolean unattestedOnly) {
        Condition condition = ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId);
        if(unattestedOnly) {
            condition = condition.and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());
        }

        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    /**
     * find historically completed attestations for all parent refs which are currently pending attestation
     * by the provided user
     * @param userId  id of user
     * @return List of attestation instances
     */
    public List<AttestationInstance> findHistoricalForPendingByUserId(String userId) {
        // fetch the parent refs for attestations currently pending for the user
        Select<Record2<String, Long>> currentlyPendingAttestationParentRefs = dsl
                .selectDistinct(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND, ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());

        // get the historically completed attestations based on the above list of parent refs
        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .innerJoin(currentlyPendingAttestationParentRefs)
                .on(currentlyPendingAttestationParentRefs.field(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND).eq(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND)
                        .and(currentlyPendingAttestationParentRefs.field(ATTESTATION_INSTANCE.PARENT_ENTITY_ID).eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID))
                )
                .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNotNull())
                .orderBy(ATTESTATION_INSTANCE.ATTESTED_AT.desc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AttestationInstance> findByEntityReference(EntityReference ref) {
        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean attestInstance(long instanceId, String attestedBy, LocalDateTime dateTime) {
        return dsl
                .update(ATTESTATION_INSTANCE)
                .set(ATTESTATION_INSTANCE.ATTESTED_BY, attestedBy)
                .set(ATTESTATION_INSTANCE.ATTESTED_AT, Timestamp.valueOf(dateTime))
                .where(ATTESTATION_INSTANCE.ID.eq(instanceId).and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull()))
                .execute() == 1;
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int cleanupOrphans() {

        Select<Record1<Long>> orphanAttestationIds = DSL
                .selectDistinct(ATTESTATION_INSTANCE.ID)
                .from(ATTESTATION_INSTANCE)
                .leftJoin(APPLICATION)
                .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNull())
                .and(APPLICATION.ID.isNull()
                        .or(APPLICATION.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.REMOVED.name()))
                        .or(APPLICATION.IS_REMOVED.eq(true)));

        dsl.deleteFrom(ATTESTATION_INSTANCE_RECIPIENT)
                .where(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.in(orphanAttestationIds))
                .execute();

        int numberOfInstancesDeleted = dsl.deleteFrom(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.ID.in(orphanAttestationIds))
                .execute();

        return numberOfInstancesDeleted;
    }


    public SyncRecipientsResponse reassignRecipients() {

        Set<Tuple2<Long, Long>> runIdToInvKindId = getAttestationRunIdToInvKindId();

        CommonTableExpression<Record2<Long, String>> runWithRequiredPeople = getRunsWithRequiredPeopleCTE(runIdToInvKindId);
        CommonTableExpression<Record2<Long, String>> missingRecipients = getMissingRecipientsCTE(runWithRequiredPeople);
        CommonTableExpression<Record2<Long, String>> recipientsToRemove = getRecipientsToRemoveCTE(runWithRequiredPeople);
        CommonTableExpression<Record1<Long>> attestationRecipientIds = getIdsForAttestationsToRemoveCTE(recipientsToRemove);

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();

                    int addedRecipients = tx
                            .with(runWithRequiredPeople)
                            .with(missingRecipients)
                            .insertInto(ATTESTATION_INSTANCE_RECIPIENT)
                            .columns(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID, ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                            .select(DSL
                                    .select(missingRecipients.field("instance_id", Long.class),
                                            missingRecipients.field(missingRecipients.field("user_id", String.class)))
                                    .from(missingRecipients))
                            .execute();

                    int removedRecipients = tx
                            .with(runWithRequiredPeople)
                            .with(recipientsToRemove)
                            .with(attestationRecipientIds)
                            .delete(ATTESTATION_INSTANCE_RECIPIENT)
                            .where(ATTESTATION_INSTANCE_RECIPIENT.ID.in(DSL
                                    .select(attestationRecipientIds.field("id", Long.class))
                                    .from(attestationRecipientIds)))
                            .execute();

                    LOG.info(format("Reassigning recipients for attestations: %d recipients added, %d recipients removed",
                            addedRecipients,
                            removedRecipients));

                    return ImmutableSyncRecipientsResponse.builder()
                            .recipientsRemovedCount(Long.valueOf(removedRecipients))
                            .recipientsCreatedCount(Long.valueOf(addedRecipients))
                            .build();
                });
    }

    private Set<Tuple2<Long, Long>> getAttestationRunIdToInvKindId() {
        return dsl
                .select(ATTESTATION_RUN.ID, INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(ATTESTATION_RUN)
                .innerJoin(INVOLVEMENT_GROUP_ENTRY)
                .on(ATTESTATION_RUN.RECIPIENT_INVOLVEMENT_GROUP_ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(ATTESTATION_RUN.RECIPIENT_INVOLVEMENT_GROUP_ID.isNotNull())
                .fetchSet(r -> tuple(r.get(ATTESTATION_RUN.ID), r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)));
    }


    public SyncRecipientsResponse getCountsOfRecipientsToReassign() {

        Set<Tuple2<Long, Long>> runIdToInvKindId = getAttestationRunIdToInvKindId();

        CommonTableExpression<Record2<Long, String>> runWithRequiredPeople = getRunsWithRequiredPeopleCTE(runIdToInvKindId);
        CommonTableExpression<Record2<Long, String>> missingRecipients = getMissingRecipientsCTE(runWithRequiredPeople);
        CommonTableExpression<Record2<Long, String>> recipientsToRemove = getRecipientsToRemoveCTE(runWithRequiredPeople);
        CommonTableExpression<Record1<Long>> attestationRecipientIds = getIdsForAttestationsToRemoveCTE(recipientsToRemove);

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();

                    Result<Record2<Long, String>> toAdd = tx
                            .with(runWithRequiredPeople)
                            .with(missingRecipients)
                            .select(missingRecipients.field("instance_id", Long.class),
                                    missingRecipients.field("user_id", String.class))
                            .from(missingRecipients)
                            .fetch();

                    Result<Record1<Long>> toRemove = tx
                            .with(runWithRequiredPeople)
                            .with(recipientsToRemove)
                            .with(attestationRecipientIds)
                            .select(attestationRecipientIds.field("id", Long.class))
                            .from(attestationRecipientIds)
                            .fetch();

                    return ImmutableSyncRecipientsResponse.builder()
                            .recipientsRemovedCount(Long.valueOf(toRemove.size()))
                            .recipientsCreatedCount(Long.valueOf(toAdd.size()))
                            .build();
        });
    }


    public List<AttestationInstance> findForEntityByRecipient(AttestEntityCommand command,
                                                              String userId,
                                                              boolean unattestedOnly) {
        Condition maybeUnattestedOnlyCondition = unattestedOnly
                ? ATTESTATION_INSTANCE.ATTESTED_AT.isNull()
                : DSL.trueCondition();

        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .and(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.eq(command.attestedEntityKind().name())
                .and(command.attestedEntityId() == null
                        ? DSL.trueCondition()
                        : ATTESTATION_RUN.ATTESTED_ENTITY_ID.eq(command.attestedEntityId()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(command.entityReference().id()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(command.entityReference().kind().name())))
                .and(maybeUnattestedOnlyCondition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AttestationInstance> findByIdSelector(Select<Record1<Long>> selector) {
        return dsl
                .select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .where(ATTESTATION_INSTANCE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Set<LatestMeasurableAttestationInfo> findLatestMeasurableAttestations(EntityReference ref){

        Field<Long> latest_attestation = DSL
                .firstValue(ATTESTATION_INSTANCE.ID)
                .over()
                .partitionBy(ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .orderBy(ATTESTATION_INSTANCE.ATTESTED_AT.desc().nullsLast())
                .as("latest_attestation");

        Table<Record> attestationsWithCategory = dsl
                .select(latest_attestation,
                        MEASURABLE_CATEGORY.ID.as("category_id"),
                        MEASURABLE_CATEGORY.NAME.as("category_name"),
                        MEASURABLE_CATEGORY.DESCRIPTION)
                .select(ATTESTATION_INSTANCE.ID.as("instance_id"),
                        ATTESTATION_INSTANCE.ATTESTED_AT,
                        ATTESTATION_INSTANCE.ATTESTED_BY)
                .select(ATTESTATION_RUN.ID.as("run_id"),
                        ATTESTATION_RUN.NAME.as("run_name"),
                        ATTESTATION_RUN.ISSUED_ON,
                        ATTESTATION_RUN.DUE_DATE)
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .innerJoin(MEASURABLE_CATEGORY)
                .on(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.eq(EntityKind.MEASURABLE_CATEGORY.name())
                        .and(ATTESTATION_RUN.ATTESTED_ENTITY_ID.eq(MEASURABLE_CATEGORY.ID)))
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(ref.id())
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(ref.kind().name())))
                .asTable();

        Result<Record> latestAttestationsWithCategory = dsl
                .select(attestationsWithCategory.fields())
                .from(attestationsWithCategory)
                .where(attestationsWithCategory.field(latest_attestation)
                        .eq(attestationsWithCategory.field("instance_id", Long.class)))
                .fetch();

        return latestAttestationsWithCategory
                .stream()
                .map(r -> {

                    EntityReference categoryRef = mkRef(
                            EntityKind.MEASURABLE_CATEGORY,
                            r.get("category_id", Long.class),
                            r.get("category_name", String.class),
                            r.get(MEASURABLE_CATEGORY.DESCRIPTION));

                    EntityReference instanceRef = mkRef(EntityKind.ATTESTATION, r.get("instance_id", Long.class));

                    EntityReference runRef = mkRef(
                            EntityKind.ATTESTATION_RUN,
                            r.get("run_id", Long.class),
                            r.get("run_name", String.class));

                    ImmutableLatestMeasurableAttestationInfo latestMeasurableAttestationInfo = ImmutableLatestMeasurableAttestationInfo.builder()
                            .categoryRef(categoryRef)
                            .attestationInstanceRef(instanceRef)
                            .attestationRunRef(runRef)
                            .issuedOn(r.get(ATTESTATION_RUN.ISSUED_ON))
                            .dueDate(r.get(ATTESTATION_RUN.DUE_DATE))
                            .attestedAt(r.get(ATTESTATION_INSTANCE.ATTESTED_AT))
                            .attestedBy(r.get(ATTESTATION_INSTANCE.ATTESTED_BY))
                            .build();

                    return latestMeasurableAttestationInfo;
                })
                .collect(toSet());
    }


    public Set<ApplicationAttestationInstanceSummary> findApplicationAttestationInstancesForKindAndSelector(EntityKind attestedKind,
                                                                                                            Long attestedId,
                                                                                                            Select<Record1<Long>> appSelector,
                                                                                                            Condition filterCondition){

        Condition attestedEntityIdCondition = attestedId == null
                ? ATTESTATION_RUN.ATTESTED_ENTITY_ID.isNull()
                : ATTESTATION_RUN.ATTESTED_ENTITY_ID.eq(attestedId);

        Field<Long> latest_attestation = DSL
                .firstValue(ATTESTATION_INSTANCE.ID)
                .over()
                .partitionBy(ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .orderBy(ATTESTATION_INSTANCE.ATTESTED_AT.desc().nullsLast())
                .as("latest_attestation");

        SelectConditionStep<Record> attestations = dsl
                .select(latest_attestation)
                .select(ATTESTATION_INSTANCE.ID.as("instance_id"),
                        ATTESTATION_INSTANCE.ATTESTED_AT,
                        ATTESTATION_INSTANCE.ATTESTED_BY,
                        ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(APPLICATION)
                .on(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(APPLICATION.ID)))
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .where(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.eq(attestedKind.name())
                        .and(attestedEntityIdCondition));

        Table<Record3<Timestamp, String, Long>> appAttestations = dsl
                .select(attestations.field(ATTESTATION_INSTANCE.ATTESTED_AT).as("attested_at"),
                        attestations.field(ATTESTATION_INSTANCE.ATTESTED_BY).as("attested_by"),
                        attestations.field(ATTESTATION_INSTANCE.PARENT_ENTITY_ID).as("appId"))
                .from(attestations)
                .where(attestations.field(latest_attestation).eq(attestations.field("instance_id", Long.class)))
                .asTable();

        Result<Record> applicationsWithLatestInstance = dsl
                .select(APPLICATION.ID,
                        APPLICATION.NAME,
                        APPLICATION.BUSINESS_CRITICALITY,
                        APPLICATION.LIFECYCLE_PHASE,
                        APPLICATION.KIND,
                        APPLICATION.ASSET_CODE)
                .select(appAttestations.field("attested_at", Timestamp.class),
                        appAttestations.field("attested_by", String.class))
                .from(APPLICATION)
                .leftJoin(appAttestations)
                .on(APPLICATION.ID.eq(appAttestations.field("appId", Long.class)))
                .where(APPLICATION.ID.in(appSelector))
                .and(filterCondition)
                .fetch();

        return applicationsWithLatestInstance
                .stream()
                .map(r -> ImmutableApplicationAttestationInstanceSummary
                        .builder()
                        .appRef(mkRef(EntityKind.APPLICATION, r.get(APPLICATION.ID), r.get(APPLICATION.NAME)))
                        .appAssetCode(r.get(APPLICATION.ASSET_CODE))
                        .appKind(ApplicationKind.valueOf(r.get(APPLICATION.KIND)))
                        .appLifecyclePhase(LifecyclePhase.valueOf(r.get(APPLICATION.LIFECYCLE_PHASE)))
                        .appCriticality(Criticality.valueOf(r.get(APPLICATION.BUSINESS_CRITICALITY)))
                        .attestedAt(r.get("attested_at", Timestamp.class))
                        .attestedBy(r.get("attested_by", String.class))
                        .build())
                .collect(toSet());
    }



    public Set<ApplicationAttestationSummaryCounts> findAttestationInstanceSummaryForSelector(Select<Record1<Long>> appSelector,
                                                                                              Condition filterCondition) {

        Table<Record5<String, Long, Timestamp, String, Long>> appAttestations = getLatestAttestationsForApps();

        Field<String> isAttestedField = DSL
                .when(appAttestations.field("attested_at", Timestamp.class).isNull(), DSL.val("NEVER_ATTESTED"))
                .otherwise(DSL.val("ATTESTED")).as("is_attested");

        SelectJoinStep<Record2<String, Long>> possibleAttestationKinds = DSL
                .selectDistinct(ATTESTATION_RUN.ATTESTED_ENTITY_KIND, ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_RUN);

        Condition attestationExistsForThisTargetEntityAndAppCondition = APPLICATION.ID.eq(appAttestations.field("appId", Long.class))
                .and(possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_KIND).eq(appAttestations.field("attested_entity_kind", String.class))
                        .and((possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_ID).eq(appAttestations.field("attested_entity_id", Long.class)))
                                .or(possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_ID).isNull().and(appAttestations.field("attested_entity_id", Long.class).isNull()))));

        //For each combination of app and possible attestation target entity, join the existing attestation instance
        Map<Tuple3<EntityKind, Long, String>, Long> appCountsForAttestationTargetEntityAndAttestationStatus = dsl
                .select(possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_KIND),
                        possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_ID),
                        isAttestedField,
                        APPLICATION.ID)
                .from(APPLICATION)
                .crossJoin(possibleAttestationKinds)
                .leftJoin(appAttestations)
                .on(attestationExistsForThisTargetEntityAndAppCondition)
                .where(APPLICATION.ID.in(appSelector))
                .and(dsl.renderInlined(filterCondition))
                .fetch(r -> tuple(
                        EntityKind.valueOf(r.get(possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_KIND))),
                        r.get(possibleAttestationKinds.field(ATTESTATION_RUN.ATTESTED_ENTITY_ID)),
                        r.get(isAttestedField),
                        r.get(APPLICATION.ID)))
                .stream()
                .collect(groupingBy(Tuple4::limit3, counting()));

        Map<Tuple2<EntityKind, Long>, Set<AttestationCount>> attestationCountsByTargetEntity = appCountsForAttestationTargetEntityAndAttestationStatus
                .entrySet()
                .stream()
                .collect(groupingBy(
                        entry -> entry.getKey().limit2(),
                        mapping(entry -> (AttestationCount) ImmutableAttestationCount.builder()
                                .key(entry.getKey().v3)
                                .count(entry.getValue().intValue())
                                .build(),
                                toSet())));

        return attestationCountsByTargetEntity
                .entrySet()
                .stream()
                .map(e -> ImmutableApplicationAttestationSummaryCounts.builder()
                        .attestedKind(e.getKey().v1)
                        .attestedId(e.getKey().v2)
                        .attestationCounts(e.getValue())
                        .build())
                .collect(toSet());
    }


    private Table<Record5<String, Long, Timestamp, String, Long>> getLatestAttestationsForApps() {

        Field<Long> latest_attestation = DSL
                .firstValue(ATTESTATION_INSTANCE.ID)
                .over()
                .partitionBy(ATTESTATION_INSTANCE.PARENT_ENTITY_ID, ATTESTATION_RUN.ATTESTED_ENTITY_KIND, ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .orderBy(ATTESTATION_INSTANCE.ATTESTED_AT.desc().nullsLast())
                .as("latest_attestation");

        SelectOnConditionStep<Record> attestations = dsl
                .select(latest_attestation)
                .select(ATTESTATION_INSTANCE.ID.as("instance_id"),
                        ATTESTATION_INSTANCE.ATTESTED_AT,
                        ATTESTATION_INSTANCE.ATTESTED_BY,
                        ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_KIND,
                        ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(APPLICATION)
                .on(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(APPLICATION.ID)))
                .innerJoin(ATTESTATION_RUN)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID));

        return dsl
                .select(attestations.field(ATTESTATION_RUN.ATTESTED_ENTITY_KIND).as("attested_entity_kind"),
                        attestations.field(ATTESTATION_RUN.ATTESTED_ENTITY_ID).as("attested_entity_id"),
                        attestations.field(ATTESTATION_INSTANCE.ATTESTED_AT).as("attested_at"),
                        attestations.field(ATTESTATION_INSTANCE.ATTESTED_BY).as("attested_by"),
                        attestations.field(ATTESTATION_INSTANCE.PARENT_ENTITY_ID).as("appId"))
                .from(attestations)
                .where(attestations.field(latest_attestation).eq(attestations.field("instance_id", Long.class)))
                .asTable();
    }


    private CommonTableExpression<Record1<Long>> getIdsForAttestationsToRemoveCTE(CommonTableExpression<Record2<Long, String>> recipientsToRemove) {
        return DSL
                .name("attestationRecipientIds")
                .fields("id")
                .as(DSL
                        .select(ATTESTATION_INSTANCE_RECIPIENT.ID)
                        .from(ATTESTATION_INSTANCE_RECIPIENT)
                        .innerJoin(recipientsToRemove)
                        .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(recipientsToRemove.field("instance_id", Long.class))
                                .and(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(recipientsToRemove.field("user_id", String.class)))));
    }


    private CommonTableExpression<Record2<Long, String>> getRecipientsToRemoveCTE(CommonTableExpression<Record2<Long, String>> runWithRequiredPeople) {
        return DSL
                .name("recipientsToRemove")
                .fields("instance_id", "user_id")
                .as(DSL
                        .select(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID,
                                ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                        .from(ATTESTATION_INSTANCE_RECIPIENT)
                        .innerJoin(ATTESTATION_INSTANCE)
                        .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                        .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNull())
                        .except(DSL
                                .select(runWithRequiredPeople.field("instance_id", Long.class),
                                        runWithRequiredPeople.field("user_id", String.class))
                                .from(runWithRequiredPeople)));
    }


    private CommonTableExpression<Record2<Long, String>> getMissingRecipientsCTE(CommonTableExpression<Record2<Long, String>> runWithRequiredPeople) {
        return DSL
                .name("missingRecipients")
                .fields("instance_id", "user_id")
                .as(DSL
                        .select(runWithRequiredPeople.field("instance_id", Long.class),
                                runWithRequiredPeople.field("user_id", String.class))
                        .from(runWithRequiredPeople)
                        .except(DSL
                                .select(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID,
                                        ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                                .from(ATTESTATION_INSTANCE_RECIPIENT)));
    }


    private CommonTableExpression<Record2<Long, String>> getRunsWithRequiredPeopleCTE(Set<Tuple2<Long, Long>> attestationRunsWithInvKind) {

        Field<Long> runId = DSL.field("runId", Long.class);
        Field<Long> kindId = DSL.field("kindId", Long.class);

        Row2<Long, Long>[] rows = attestationRunsWithInvKind
                .stream()
                .map(t -> DSL.row(t.v1, t.v2))
                .collect(toSet())
                .toArray(new Row2[0]);

        Table<Record2<Long, Long>> runToKindId = DSL
                .select(runId, kindId)
                .from(DSL
                        .values(rows).as("runToKind", "runId", "kindId"))
                .asTable();

        return DSL
                .name("runWithRequiredPeople")
                .fields("instance_id", "user_id")
                .as(DSL
                        .selectDistinct(ATTESTATION_INSTANCE.ID, PERSON.EMAIL)
                        .from(ATTESTATION_INSTANCE)
                        .innerJoin(INVOLVEMENT)
                        .on(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(INVOLVEMENT.ENTITY_ID)
                                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(INVOLVEMENT.ENTITY_KIND)))
                        .innerJoin(PERSON).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID)
                                .and(PERSON.IS_REMOVED.isFalse()))
                        .innerJoin(runToKindId).on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runToKindId.field(runId)))
                        .and(INVOLVEMENT.KIND_ID.eq(runToKindId.field(kindId)))
                        .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNull()));
    }

}