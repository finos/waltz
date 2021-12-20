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
import org.finos.waltz.model.survey.*;
import org.finos.waltz.schema.tables.records.SurveyInstanceRecipientRecord;
import org.finos.waltz.schema.tables.records.SurveyInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.*;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.JooqUtilities.maybeReadRef;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class SurveyInstanceDao {

    private static final org.finos.waltz.schema.tables.SurveyInstance si = SURVEY_INSTANCE;

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
        record.setDueDate(command.dueDate().map(Date::valueOf).orElse(null));
        record.setApprovalDueDate(toSqlDate(command.approvalDueDate()));
        record.setOwningRole(command.owningRole());

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
}
