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

package org.finos.waltz.data.survey;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.attestation.ImmutableSyncRecipientsResponse;
import org.finos.waltz.model.attestation.SyncRecipientsResponse;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.finos.waltz.schema.tables.records.SurveyInstanceRecipientRecord;
import org.finos.waltz.schema.tables.records.SurveyInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.*;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.data.JooqUtilities.maybeReadRef;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class SurveyInstanceDao {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstanceDao.class);

    private static final org.finos.waltz.schema.tables.SurveyInstance si = SURVEY_INSTANCE;
    private static final org.finos.waltz.schema.tables.SurveyRun sr = SURVEY_RUN;
    private static final org.finos.waltz.schema.tables.SurveyInstanceRecipient sir = SURVEY_INSTANCE_RECIPIENT;
    private static final org.finos.waltz.schema.tables.SurveyInstanceOwner sio = SURVEY_INSTANCE_OWNER;
    private static final org.finos.waltz.schema.tables.SurveyTemplate st = SURVEY_TEMPLATE;
    private static final org.finos.waltz.schema.tables.InvolvementKind ik = INVOLVEMENT_KIND;
    private static final org.finos.waltz.schema.tables.Involvement i = INVOLVEMENT;
    private static final org.finos.waltz.schema.tables.Person p = PERSON;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory
            .mkNameField(
                    si.ENTITY_ID,
                    si.ENTITY_KIND,
                    newArrayList(EntityKind.values()))
            .as("entity_name");

    private static final Field<String> EXTERNAL_ID_FIELD = InlineSelectFieldFactory
            .mkExternalIdField(
                    si.ENTITY_ID,
                    si.ENTITY_KIND,
                    newArrayList(EntityKind.values()))
            .as("external_id");


    private static final Condition IS_ORIGINAL_INSTANCE_CONDITION = si.ORIGINAL_INSTANCE_ID.isNull();

    private static final RecordMapper<Record, SurveyInstance> TO_DOMAIN_MAPPER = r -> {
        SurveyInstanceRecord record = r.into(si);
        return ImmutableSurveyInstance.builder()
                .id(record.getId())
                .surveyRunId(record.getSurveyRunId())
                .surveyEntity(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .surveyEntityExternalId(r.getValue(EXTERNAL_ID_FIELD))
                .status(SurveyInstanceStatus.valueOf(record.getStatus()))
                .dueDate(toLocalDate(record.getDueDate()))
                .approvalDueDate(toLocalDate(record.getApprovalDueDate()))
                .submittedAt(ofNullable(record.getSubmittedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .submittedBy(record.getSubmittedBy())
                .approvedAt(ofNullable(record.getApprovedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .approvedBy(record.getApprovedBy())
                .originalInstanceId(record.getOriginalInstanceId())
                .owningRole(record.getOwningRole())
                .name(record.getName())
                .qualifierEntity(maybeReadRef(
                        record,
                        si.ENTITY_QUALIFIER_KIND,
                        si.ENTITY_QUALIFIER_ID)
                        .orElse(null))
                .issuedOn(toLocalDate(record.getIssuedOn()))
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public SurveyInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyInstance getById(long id) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .where(si.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Set<SurveyInstance> findForRecipient(long personId) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(si.ID))
                .where(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<SurveyInstance> findForSurveyRun(long surveyRunId) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .where(si.SURVEY_RUN_ID.eq(surveyRunId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public long create(SurveyInstanceCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyInstanceRecord record = dsl.newRecord(si);
        record.setSurveyRunId(command.surveyRunId());
        record.setEntityKind(command.entityReference().kind().name());
        record.setEntityId(command.entityReference().id());
        record.setStatus(command.status().name());
        record.setDueDate(toSqlDate(command.dueDate()));
        record.setApprovalDueDate(toSqlDate(command.approvalDueDate()));
        record.setOwningRole(command.owningRole());
        record.setName(command.name());
        record.setIssuedOn(toSqlDate(command.issuedOn()));

        record.store();
        return record.getId();
    }


    public long createPreviousVersion(SurveyInstance currentInstance) {
        checkNotNull(currentInstance, "currentInstance cannot be null");

        SurveyInstanceRecord record = dsl.newRecord(si);
        record.setSurveyRunId(currentInstance.surveyRunId());
        record.setEntityKind(currentInstance.surveyEntity().kind().name());
        record.setEntityId(currentInstance.surveyEntity().id());
        record.setStatus(currentInstance.status().name());
        record.setDueDate(toSqlDate(currentInstance.dueDate()));
        record.setApprovalDueDate(toSqlDate(currentInstance.approvalDueDate()));
        record.setOriginalInstanceId(currentInstance.id().get());
        record.setSubmittedAt((currentInstance.submittedAt() != null) ? Timestamp.valueOf(currentInstance.submittedAt()) : null);
        record.setSubmittedBy(currentInstance.submittedBy());
        record.setApprovedAt(ofNullable(currentInstance.approvedAt())
                .map(dt -> Timestamp.valueOf(currentInstance.approvedAt()))
                .orElse(null));
        record.setApprovedBy(currentInstance.approvedBy());
        record.setOwningRole(currentInstance.owningRole());
        record.setName(currentInstance.name());
        Optional
                .ofNullable(currentInstance.qualifierEntity())
                .ifPresent(ref -> {
                    record.setEntityQualifierKind(ref.kind().name());
                    record.setEntityQualifierId(ref.id());
                });
        record.setIssuedOn(toSqlDate(today()));

        record.store();
        return record.getId();
    }


    public int deleteForSurveyRun(long surveyRunId) {
        return dsl.delete(si)
                .where(si.SURVEY_RUN_ID.eq(surveyRunId))
                .execute();
    }


    public int updateStatus(long instanceId, SurveyInstanceStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return dsl.update(si)
                .set(si.STATUS, newStatus.name())
                .where(si.STATUS.notEqual(newStatus.name())
                        .and(si.ID.eq(instanceId)))
                .execute();
    }


    public int updateSubmissionDueDate(long instanceId, LocalDate newDueDate) {
        return dsl.update(si)
                .set(si.DUE_DATE, toSqlDate(newDueDate))
                .where(si.ID.eq(instanceId))
                .execute();
    }


    public int updateApprovalDueDate(long instanceId, LocalDate newDueDate) {
        return dsl.update(si)
                .set(si.APPROVAL_DUE_DATE, toSqlDate(newDueDate))
                .where(si.ID.eq(instanceId))
                .execute();
    }


    public int updateDueDateForSurveyRun(long surveyRunId, LocalDate newDueDate) {
        return dsl.update(si)
                .set(si.DUE_DATE, toSqlDate(newDueDate))
                .where(si.SURVEY_RUN_ID.eq(surveyRunId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .execute();
    }


    public int updateApprovalDueDateForSurveyRun(long surveyRunId, LocalDate newDueDate) {
        return dsl.update(si)
                .set(si.APPROVAL_DUE_DATE, toSqlDate(newDueDate))
                .where(si.SURVEY_RUN_ID.eq(surveyRunId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .execute();
    }


    public int updateOwningRoleForSurveyRun(long surveyRunId, String role) {
        return dsl.update(si)
                .set(si.OWNING_ROLE, role)
                .where(si.SURVEY_RUN_ID.eq(surveyRunId))
                .execute();
    }


    public int updateSubmitted(long instanceId, String userName) {
        checkNotNull(userName, "userName cannot be null");

        return dsl.update(si)
                .set(si.SUBMITTED_AT, Timestamp.valueOf(nowUtc()))
                .set(si.SUBMITTED_BY, userName)
                .where(si.ID.eq(instanceId))
                .execute();
    }


    public int markApproved(long instanceId, String userName) {
        checkNotNull(userName, "userName cannot be null");

        return dsl.update(si)
                .set(si.APPROVED_AT, Timestamp.valueOf(nowUtc()))
                .set(si.APPROVED_BY, userName)
                .set(si.STATUS, SurveyInstanceStatus.APPROVED.name())
                .where(si.ID.eq(instanceId))
                .execute();
    }


    public void clearApproved(long instanceId) {
        dsl.update(si)
                .set(si.APPROVED_AT, (Timestamp) null)
                .set(si.APPROVED_BY, (String) null)
                .where(si.ID.eq(instanceId))
                .execute();
    }


    public List<SurveyInstance> findBySurveyInstanceIdSelector(Select<Record1<Long>> selector) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .where(si.ID.in(selector))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public List<SurveyInstance> findPreviousVersionsForInstance(long instanceId) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .where(si.ORIGINAL_INSTANCE_ID.eq(instanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findVersionsForInstance(long instanceId) {

        SelectConditionStep<Record1<Long>> origId = DSL
                .select(DSL.coalesce(si.ORIGINAL_INSTANCE_ID, si.ID))
                .from(si)
                .where(si.ID.eq(instanceId));

        return dsl
                .select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .where(si.ID.eq(origId))
                .or(si.ORIGINAL_INSTANCE_ID.eq(origId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public SurveyRunCompletionRate getCompletionRateForSurveyRun(Long surveyRunId) {
        Condition condition = si.SURVEY_RUN_ID.eq(surveyRunId);
        return CollectionUtilities
                .maybeFirst(calcCompletionRateForSurveyRuns(condition))
                .orElse(null);
    }

    public List<SurveyRunCompletionRate> findCompletionRateForSurveyTemplate(Long surveyTemplateId) {
        Condition condition = si.SURVEY_RUN_ID.in(DSL
                .select(SURVEY_RUN.ID)
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(surveyTemplateId)));

        return calcCompletionRateForSurveyRuns(condition);
    }


    private List<SurveyRunCompletionRate> calcCompletionRateForSurveyRuns(Condition surveyRunSelectionCondition) {
        Field<Integer> statCount = DSL.count(si.ID).as("statCount");

        final Result<Record3<Long, String, Integer>> countsByRunAndStatus = dsl
                .select(si.SURVEY_RUN_ID,
                        si.STATUS,
                        statCount)
                .from(si)
                .where(surveyRunSelectionCondition)
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .groupBy(si.SURVEY_RUN_ID, si.STATUS)
                .fetch();

        Map<Long, ImmutableSurveyRunCompletionRate.Builder> buildersByRunId = new HashMap<>();
        countsByRunAndStatus.forEach(r -> {
            Long runId = r.get(si.SURVEY_RUN_ID);
            ImmutableSurveyRunCompletionRate.Builder inProgressBuilder = buildersByRunId.getOrDefault(
                    runId,
                    ImmutableSurveyRunCompletionRate.builder().surveyRunId(runId));
            if (isStatTypeOf(r, SurveyInstanceStatus.NOT_STARTED)) {
                inProgressBuilder.notStartedCount(r.get(statCount));
            }
            if (isStatTypeOf(r, SurveyInstanceStatus.IN_PROGRESS)) {
                inProgressBuilder.inProgressCount(r.get(statCount));
            }
            if (isStatTypeOf(r, SurveyInstanceStatus.COMPLETED)) {
                inProgressBuilder.completedCount(r.get(statCount));
            }

            buildersByRunId.put(runId, inProgressBuilder);
        });

        return buildersByRunId
                .values()
                .stream()
                .map(ImmutableSurveyRunCompletionRate.Builder::build)
                .collect(Collectors.toList());
    }


    private boolean isStatTypeOf(Record3<Long, String, Integer> r, SurveyInstanceStatus status) {
        return r.get(si.STATUS).equals(status.name());
    }


    private int getCountByStatus(Result<Record2<String, Integer>> countsByStatus, SurveyInstanceStatus status) {
        return countsByStatus.stream()
                .filter(r -> status.name().equals(r.value1()))
                .findAny()
                .map(Record2::value2)
                .orElse(0);

    }

    public int[] createInstanceRecipients(Long instanceId, Collection<Long> personIds) {
        Collection<SurveyInstanceRecipientRecord> records = CollectionUtilities.map(
                personIds,
                p -> {
                    SurveyInstanceRecipientRecord record = new SurveyInstanceRecipientRecord();
                    record.setSurveyInstanceId(instanceId);
                    record.setPersonId(p);
                    return record;
                });

        return dsl.batchInsert(records).execute();
    }

    public Set<SurveyInstance> findForOwner(Long personId) {
        return dsl.select(si.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(si)
                .innerJoin(SURVEY_INSTANCE_OWNER)
                .on(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(si.ID))
                .where(SURVEY_INSTANCE_OWNER.PERSON_ID.eq(personId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public SyncRecipientsResponse getReassignRecipientsCounts() {

        CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys = getInScopeSurveysCTE();
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredRecipients = getRequiredRecipientsCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingRecipients = getExistingRecipientsCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToRemove = getMembersToRemoveCTE(existingRecipients, requiredRecipients);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToAdd = getMembersToAddCTE(existingRecipients, requiredRecipients);

        Result<Record6<Long, Long, String, Long, String, String>> countToRemove = dsl
                .with(inScopeSurveys)
                .with(requiredRecipients)
                .with(existingRecipients)
                .with(recipientsToRemove)
                .selectFrom(recipientsToRemove)
                .fetch();

        Result<Record6<Long, Long, String, Long, String, String>> countToAdd = dsl
                .with(inScopeSurveys)
                .with(requiredRecipients)
                .with(existingRecipients)
                .with(recipientsToAdd)
                .selectFrom(recipientsToAdd)
                .fetch();

        return ImmutableSyncRecipientsResponse
                .builder()
                .recipientsCreatedCount((long) countToAdd.size())
                .recipientsRemovedCount((long) countToRemove.size())
                .build();
    }


    public SyncRecipientsResponse getReassignOwnersCounts() {

        CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys = getInScopeSurveysCTE();
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredOwners = getRequiredOwnersCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingOwners = getExistingOwnersCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToRemove = getMembersToRemoveCTE(existingOwners, requiredOwners);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToAdd = getMembersToAddCTE(existingOwners, requiredOwners);

        Result<Record6<Long, Long, String, Long, String, String>> countToRemove = dsl
                .with(inScopeSurveys)
                .with(requiredOwners)
                .with(existingOwners)
                .with(ownersToRemove)
                .selectFrom(ownersToRemove)
                .fetch();

        Result<Record6<Long, Long, String, Long, String, String>> countToAdd = dsl
                .with(inScopeSurveys)
                .with(requiredOwners)
                .with(existingOwners)
                .with(ownersToAdd)
                .selectFrom(ownersToAdd)
                .fetch();

        return ImmutableSyncRecipientsResponse
                .builder()
                .recipientsCreatedCount((long) countToAdd.size())
                .recipientsRemovedCount((long) countToRemove.size())
                .build();
    }


    public SyncRecipientsResponse reassignRecipients() {

        CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys = getInScopeSurveysCTE();
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredRecipients = getRequiredRecipientsCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingRecipients = getExistingRecipientsCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToRemove = getMembersToRemoveCTE(existingRecipients, requiredRecipients);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToAdd = getMembersToAddCTE(existingRecipients, requiredRecipients);

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();

                    LOG.debug("Creating change logs for additions and removals");
                    int[] removalChangelogs = createRemovalChangeLogs(tx, inScopeSurveys, requiredRecipients, existingRecipients, recipientsToRemove, SurveyInvolvementKind.RECIPIENT);
                    int[] additionChangelogs = createAdditionChangeLogs(tx, inScopeSurveys, requiredRecipients, existingRecipients, recipientsToAdd, SurveyInvolvementKind.RECIPIENT);

                    LOG.debug("Creating new recipients");
                    int insertedRecords = insertRecipients(tx, inScopeSurveys, requiredRecipients, existingRecipients, recipientsToRemove, recipientsToAdd);

                    LOG.debug("Removing recipients");
                    int removedRecords = removeRecipients(tx, inScopeSurveys, requiredRecipients, existingRecipients, recipientsToRemove);

                    LOG.debug(format("Created [%d] recipients and [%d] addition changelogs, removed [%d] recipients who are no longer active and [%d] removal changelogs",
                            insertedRecords,
                            IntStream.of(additionChangelogs).sum(),
                            removedRecords,
                            IntStream.of(removalChangelogs).sum()));

                    return ImmutableSyncRecipientsResponse
                            .builder()
                            .recipientsCreatedCount((long) insertedRecords)
                            .recipientsRemovedCount((long) removedRecords)
                            .build();
                });
    }

    public SyncRecipientsResponse reassignOwners() {

        CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys = getInScopeSurveysCTE();
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredOwners = getRequiredOwnersCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingOwners = getExistingOwnersCTE(inScopeSurveys);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToRemove = getMembersToRemoveCTE(existingOwners, requiredOwners);
        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToAdd = getMembersToAddCTE(existingOwners, requiredOwners);

        return dsl
                .transactionResult(ctx -> {
                    DSLContext tx = ctx.dsl();

                    LOG.debug("Creating change logs for additions and removals");
                    int[] removalChangelogs = createRemovalChangeLogs(tx, inScopeSurveys, requiredOwners, existingOwners, ownersToRemove, SurveyInvolvementKind.OWNER);
                    int[] additionChangelogs = createAdditionChangeLogs(tx, inScopeSurveys, requiredOwners, existingOwners, ownersToAdd, SurveyInvolvementKind.OWNER);

                    LOG.debug("Creating new owners");
                    int insertedRecords = insertOwners(tx, inScopeSurveys, requiredOwners, existingOwners, ownersToRemove, ownersToAdd);

                    LOG.debug("Removing owners");
                    int removedRecords = removeOwners(tx, inScopeSurveys, requiredOwners, existingOwners, ownersToRemove);

                    LOG.debug(format("Created [%d] owners and [%d] addition changelogs, removed [%d] owners who are no longer active and [%d] removal changelogs",
                            insertedRecords,
                            IntStream.of(additionChangelogs).sum(),
                            removedRecords,
                            IntStream.of(removalChangelogs).sum()));

                    return ImmutableSyncRecipientsResponse
                            .builder()
                            .recipientsCreatedCount((long) insertedRecords)
                            .recipientsRemovedCount((long) removedRecords)
                            .build();
                });
    }

    private int removeRecipients(DSLContext tx,
                                 CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredRecipients,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingRecipients,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToRemove) {
        return tx
                .with(inScopeSurveys)
                .with(requiredRecipients)
                .with(existingRecipients)
                .with(recipientsToRemove)
                .delete(sir)
                .where(sir.ID.in(DSL
                        .select(sir.ID)
                        .from(sir)
                        .innerJoin(recipientsToRemove)
                        .on(sir.SURVEY_INSTANCE_ID.eq(recipientsToRemove.field("survey_instance_id", Long.class))
                                .and(sir.PERSON_ID.eq(recipientsToRemove.field("person_id", Long.class))))))
                .execute();
    }

    private int removeOwners(DSLContext tx,
                             CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredOwners,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingOwners,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToRemove) {
        return tx
                .with(inScopeSurveys)
                .with(requiredOwners)
                .with(existingOwners)
                .with(ownersToRemove)
                .delete(sio)
                .where(sio.ID.in(DSL
                        .select(sio.ID)
                        .from(sio)
                        .innerJoin(ownersToRemove)
                        .on(sio.SURVEY_INSTANCE_ID.eq(ownersToRemove.field("survey_instance_id", Long.class))
                                .and(sio.PERSON_ID.eq(ownersToRemove.field("person_id", Long.class))))))
                .execute();
    }

    private int insertRecipients(DSLContext tx,
                                 CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredRecipients,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingRecipients,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToRemove,
                                 CommonTableExpression<Record6<Long, Long, String, Long, String, String>> recipientsToAdd) {
        return tx
                .with(inScopeSurveys)
                .with(requiredRecipients)
                .with(existingRecipients)
                .with(recipientsToRemove)
                .with(recipientsToAdd)
                .insertInto(sir)
                .columns(sir.SURVEY_INSTANCE_ID, sir.PERSON_ID)
                .select(DSL
                        .select(recipientsToAdd.field("survey_instance_id", Long.class),
                                recipientsToAdd.field("person_id", Long.class))
                        .from(recipientsToAdd))
                .execute();
    }

    private int insertOwners(DSLContext tx,
                             CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredOwners,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingOwners,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToRemove,
                             CommonTableExpression<Record6<Long, Long, String, Long, String, String>> ownersToAdd) {
        return tx
                .with(inScopeSurveys)
                .with(requiredOwners)
                .with(existingOwners)
                .with(ownersToRemove)
                .with(ownersToAdd)
                .insertInto(sio)
                .columns(sio.SURVEY_INSTANCE_ID, sio.PERSON_ID)
                .select(DSL
                        .select(ownersToAdd.field("survey_instance_id", Long.class),
                                ownersToAdd.field("person_id", Long.class))
                        .from(ownersToAdd))
                .execute();
    }

    private int[] createAdditionChangeLogs(DSLContext tx,
                                           CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                                           CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredMembers,
                                           CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingMembers,
                                           CommonTableExpression<Record6<Long, Long, String, Long, String, String>> membersToAdd,
                                           SurveyInvolvementKind involvementKind) {

        Timestamp now = nowUtcTimestamp();

        return tx
                .with(inScopeSurveys)
                .with(requiredMembers)
                .with(existingMembers)
                .with(membersToAdd)
                .selectFrom(membersToAdd)
                .fetch()
                .stream()
                .map(r -> {

                    String templateName = r.get("template_name", String.class);
                    String person = r.get("person_email", String.class);
                    Long personId = r.get("person_id", Long.class);
                    Long instanceId = r.get("survey_instance_id", Long.class);
                    String entityKind = r.get("entity_kind", String.class);
                    Long entityId = r.get("entity_id", Long.class);

                    String message = format(
                            "Added %s: %s [%d] to survey: %s [%d] as they have the specified involvement kind",
                            lower(involvementKind.name()),
                            person,
                            personId,
                            templateName,
                            instanceId);

                    ChangeLogRecord clRecord = tx.newRecord(CHANGE_LOG);
                    clRecord.setParentId(entityId);
                    clRecord.setParentKind(entityKind);
                    clRecord.setChildKind(EntityKind.SURVEY_INSTANCE_RECIPIENT.name());
                    clRecord.setMessage(message);
                    clRecord.setOperation(Operation.ADD.name());
                    clRecord.setSeverity(Severity.INFORMATION.name());
                    clRecord.setCreatedAt(now);
                    clRecord.setUserId("admin");

                    return clRecord;

                })
                .collect(collectingAndThen(toSet(), tx::batchInsert))
                .execute();
    }

    private int[] createRemovalChangeLogs(DSLContext tx,
                                          CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys,
                                          CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredMembers,
                                          CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingMembers,
                                          CommonTableExpression<Record6<Long, Long, String, Long, String, String>> membersToRemove,
                                          SurveyInvolvementKind involvementKind) {

        Timestamp now = nowUtcTimestamp();

        return tx
                .with(inScopeSurveys)
                .with(requiredMembers)
                .with(existingMembers)
                .with(membersToRemove)
                .selectFrom(membersToRemove)
                .fetch()
                .stream()
                .map(r -> {

                    String templateName = r.get("template_name", String.class);
                    String person = r.get("person_email", String.class);
                    Long personId = r.get("person_id", Long.class);
                    Long instanceId = r.get("survey_instance_id", Long.class);
                    String entityKind = r.get("entity_kind", String.class);
                    Long entityId = r.get("entity_id", Long.class);

                    String messageText = "Removed %s: %s [%d] from survey: %s [%d] as they are no longer active";
                    String message = format(
                            messageText,
                            lower(involvementKind.name()),
                            person,
                            personId,
                            templateName,
                            instanceId);

                    ChangeLogRecord clRecord = tx.newRecord(CHANGE_LOG);
                    clRecord.setParentId(entityId);
                    clRecord.setParentKind(entityKind);
                    clRecord.setChildKind(EntityKind.SURVEY_INSTANCE_RECIPIENT.name());
                    clRecord.setMessage(message);
                    clRecord.setOperation(Operation.REMOVE.name());
                    clRecord.setSeverity(Severity.INFORMATION.name());
                    clRecord.setCreatedAt(now);
                    clRecord.setUserId("admin");

                    return clRecord;
                })
                .collect(collectingAndThen(toSet(), tx::batchInsert))
                .execute();
    }

    private CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getMembersToAddCTE(CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingRecipients,
                                                                                                        CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredRecipients) {
        return DSL
                .name("recipientsToAdd")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(requiredRecipients.field("survey_instance_id", Long.class),
                                requiredRecipients.field("person_id", Long.class),
                                requiredRecipients.field("entity_kind", String.class),
                                requiredRecipients.field("entity_id", Long.class),
                                requiredRecipients.field("template_name", String.class),
                                requiredRecipients.field("person_email", String.class))
                        .from(requiredRecipients)
                        .except(DSL
                                .select(existingRecipients.field("survey_instance_id", Long.class),
                                        existingRecipients.field("person_id", Long.class),
                                        existingRecipients.field("entity_kind", String.class),
                                        existingRecipients.field("entity_id", Long.class),
                                        existingRecipients.field("template_name", String.class),
                                        existingRecipients.field("person_email", String.class))
                                .from(existingRecipients)));
    }


    private CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getMembersToRemoveCTE(CommonTableExpression<Record6<Long, Long, String, Long, String, String>> existingMembers,
                                                                                                           CommonTableExpression<Record6<Long, Long, String, Long, String, String>> requiredMembers) {
        return DSL
                .name("membersToRemove")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(existingMembers.field("survey_instance_id", Long.class),
                                existingMembers.field("person_id", Long.class),
                                existingMembers.field("entity_kind", String.class),
                                existingMembers.field("entity_id", Long.class),
                                existingMembers.field("template_name", String.class),
                                existingMembers.field("person_email", String.class))
                        .from(existingMembers)
                        .innerJoin(p).on(existingMembers.field("person_id", Long.class).eq(p.ID)
                                .and(p.IS_REMOVED.isTrue())) // only want to remove members that are no longer active
                        .except(DSL
                                .select(requiredMembers.field("survey_instance_id", Long.class),
                                        requiredMembers.field("person_id", Long.class),
                                        requiredMembers.field("entity_kind", String.class),
                                        requiredMembers.field("entity_id", Long.class),
                                        requiredMembers.field("template_name", String.class),
                                        requiredMembers.field("person_email", String.class))
                                .from(requiredMembers)));
    }

    private CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getExistingRecipientsCTE(CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys) {
        return DSL
                .name("existingRecipients")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(sir.SURVEY_INSTANCE_ID,
                                sir.PERSON_ID,
                                inScopeSurveys.field(si.ENTITY_KIND),
                                inScopeSurveys.field(si.ENTITY_ID),
                                inScopeSurveys.field(st.NAME),
                                p.EMAIL)
                        .from(inScopeSurveys)
                        .innerJoin(sir).on(inScopeSurveys.field(si.ID).eq(sir.SURVEY_INSTANCE_ID))
                        .innerJoin(p).on(sir.PERSON_ID.eq(p.ID)));
    }


    private CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getExistingOwnersCTE(CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys) {
        return DSL
                .name("existingOwners")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(sio.SURVEY_INSTANCE_ID,
                                sio.PERSON_ID,
                                inScopeSurveys.field(si.ENTITY_KIND),
                                inScopeSurveys.field(si.ENTITY_ID),
                                inScopeSurveys.field(st.NAME),
                                p.EMAIL)
                        .from(inScopeSurveys)
                        .innerJoin(sio).on(inScopeSurveys.field(si.ID).eq(sio.SURVEY_INSTANCE_ID))
                        .innerJoin(p).on(sio.PERSON_ID.eq(p.ID)));
    }

    private CommonTableExpression<Record6<Long, Long, String, String, String, String>> getInScopeSurveysCTE() {
        return DSL
                .name("inScopeSurveys")
                .as(DSL
                        .select(si.ID, si.ENTITY_ID, si.ENTITY_KIND, sr.INVOLVEMENT_KIND_IDS, sr.OWNER_INV_KIND_IDS, st.NAME)
                        .from(sr)
                        .innerJoin(st).on(sr.SURVEY_TEMPLATE_ID.eq(st.ID)
                                .and(st.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name())))
                        .innerJoin(si).on(sr.ID.eq(si.SURVEY_RUN_ID)
                                .and(si.STATUS.in(
                                        SurveyInstanceStatus.NOT_STARTED.name(),
                                        SurveyInstanceStatus.IN_PROGRESS.name(),
                                        SurveyInstanceStatus.REJECTED.name()))));

    }


    public CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getRequiredRecipientsCTE(CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys) {

        Field<String> surveyInvolvementsString = DSL.concat(DSL.val(";"), inScopeSurveys.field(sr.INVOLVEMENT_KIND_IDS), DSL.val(";"));
        Field<String> involvementKindsString = DSL.concat(DSL.val("%;"), DSL.cast(ik.ID, String.class), DSL.val(";%"));

        return DSL
                .name("requiredRecipients")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(inScopeSurveys.field(si.ID),
                                p.ID,
                                inScopeSurveys.field(si.ENTITY_KIND),
                                inScopeSurveys.field(si.ENTITY_ID),
                                inScopeSurveys.field(st.NAME),
                                p.EMAIL)
                        .from(inScopeSurveys)
                        .innerJoin(ik).on(surveyInvolvementsString.like(involvementKindsString))
                        .innerJoin(i).on(ik.ID.eq(i.KIND_ID)
                                .and(i.ENTITY_KIND.eq(inScopeSurveys.field(si.ENTITY_KIND))
                                        .and(i.ENTITY_ID.eq(inScopeSurveys.field(si.ENTITY_ID)))))
                        .innerJoin(p).on(i.EMPLOYEE_ID.eq(p.EMPLOYEE_ID)
                                .and(p.IS_REMOVED.isFalse()))
                        .where(inScopeSurveys.field(sr.INVOLVEMENT_KIND_IDS).isNotNull()
                                .and(inScopeSurveys.field(sr.INVOLVEMENT_KIND_IDS).ne(""))));
    }

    private CommonTableExpression<Record6<Long, Long, String, Long, String, String>> getRequiredOwnersCTE(CommonTableExpression<Record6<Long, Long, String, String, String, String>> inScopeSurveys) {

        Field<String> surveyInvolvementsString = DSL.concat(DSL.val(";"), inScopeSurveys.field(sr.OWNER_INV_KIND_IDS), DSL.val(";"));
        Field<String> involvementKindsString = DSL.concat(DSL.val("%;"), DSL.cast(ik.ID, String.class), DSL.val(";%"));

        return DSL
                .name("requiredOwners")
                .fields("survey_instance_id", "person_id", "entity_kind", "entity_id", "template_name", "person_email")
                .as(DSL
                        .select(inScopeSurveys.field(si.ID),
                                p.ID,
                                inScopeSurveys.field(si.ENTITY_KIND),
                                inScopeSurveys.field(si.ENTITY_ID),
                                inScopeSurveys.field(st.NAME),
                                p.EMAIL)
                        .from(inScopeSurveys)
                        .innerJoin(ik).on(surveyInvolvementsString.like(involvementKindsString))
                        .innerJoin(i).on(ik.ID.eq(i.KIND_ID)
                                .and(i.ENTITY_KIND.eq(inScopeSurveys.field(si.ENTITY_KIND))
                                        .and(i.ENTITY_ID.eq(inScopeSurveys.field(si.ENTITY_ID)))))
                        .innerJoin(p).on(i.EMPLOYEE_ID.eq(p.EMPLOYEE_ID)
                                .and(p.IS_REMOVED.isFalse()))
                        .where(inScopeSurveys.field(sr.OWNER_INV_KIND_IDS).isNotNull()
                                .and(inScopeSurveys.field(sr.OWNER_INV_KIND_IDS).ne(""))));
    }
}