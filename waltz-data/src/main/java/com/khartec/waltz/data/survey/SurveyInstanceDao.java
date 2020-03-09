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

package com.khartec.waltz.data.survey;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.schema.tables.records.SurveyInstanceRecipientRecord;
import com.khartec.waltz.schema.tables.records.SurveyInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.Optional.ofNullable;

@Repository
public class SurveyInstanceDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                SURVEY_INSTANCE.ENTITY_ID,
                SURVEY_INSTANCE.ENTITY_KIND,
                newArrayList(EntityKind.values()))
            .as("entity_name");

    private static final Field<String> EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
            SURVEY_INSTANCE.ENTITY_ID,
            SURVEY_INSTANCE.ENTITY_KIND,
            newArrayList(EntityKind.values()))
            .as("external_id");


    private static final Condition IS_ORIGINAL_INSTANCE_CONDITION = SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull();

    private static final RecordMapper<Record, SurveyInstance> TO_DOMAIN_MAPPER = r -> {
        SurveyInstanceRecord record = r.into(SURVEY_INSTANCE);
        return ImmutableSurveyInstance.builder()
                .id(record.getId())
                .surveyRunId(record.getSurveyRunId())
                .surveyEntity(EntityReference.mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .surveyEntityExternalId(r.getValue(EXTERNAL_ID_FIELD))
                .status(SurveyInstanceStatus.valueOf(record.getStatus()))
                .dueDate(record.getDueDate().toLocalDate())
                .submittedAt(ofNullable(record.getSubmittedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .submittedBy(record.getSubmittedBy())
                .approvedAt(ofNullable(record.getApprovedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .approvedBy(record.getApprovedBy())
                .originalInstanceId(record.getOriginalInstanceId())
                .ownerId(record.getOwnerId())
                .owningRole(record.getOwningRole())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public SurveyInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyInstance getById(long id) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findForRecipient(long personId) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .where(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findForSurveyRun(long surveyRunId) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(SurveyInstanceCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyInstanceRecord record = dsl.newRecord(SURVEY_INSTANCE);
        record.setSurveyRunId(command.surveyRunId());
        record.setEntityKind(command.entityReference().kind().name());
        record.setEntityId(command.entityReference().id());
        record.setStatus(command.status().name());
        record.setDueDate(command.dueDate().map(Date::valueOf).orElse(null));
        record.setOwnerId(command.ownerId());
        record.setOwningRole(command.owningRole());

        record.store();
        return record.getId();
    }


    public long createPreviousVersion(SurveyInstance currentInstance) {
        checkNotNull(currentInstance, "currentInstance cannot be null");

        SurveyInstanceRecord record = dsl.newRecord(SURVEY_INSTANCE);
        record.setSurveyRunId(currentInstance.surveyRunId());
        record.setEntityKind(currentInstance.surveyEntity().kind().name());
        record.setEntityId(currentInstance.surveyEntity().id());
        record.setStatus(currentInstance.status().name());
        record.setDueDate(toSqlDate(currentInstance.dueDate()));
        record.setOriginalInstanceId(currentInstance.id().get());
        record.setSubmittedAt((currentInstance.submittedAt() != null) ? Timestamp.valueOf(currentInstance.submittedAt()) : null);
        record.setSubmittedBy(currentInstance.submittedBy());
        record.setApprovedAt(ofNullable(currentInstance.approvedAt())
                .map(dt -> Timestamp.valueOf(currentInstance.approvedAt()))
                .orElse(null));
        record.setApprovedBy(currentInstance.approvedBy());
        record.setOwnerId(currentInstance.ownerId());
        record.setOwningRole(currentInstance.owningRole());

        record.store();
        return record.getId();
    }


    public int deleteForSurveyRun(long surveyRunId) {
        return dsl.delete(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .execute();
    }


    public int updateStatus(long instanceId, SurveyInstanceStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.STATUS, newStatus.name())
                .where(SURVEY_INSTANCE.STATUS.notEqual(newStatus.name())
                        .and(SURVEY_INSTANCE.ID.eq(instanceId)))
                .execute();
    }


    public int updateDueDate(long instanceId, LocalDate newDueDate) {
        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.DUE_DATE, toSqlDate(newDueDate))
                .where(SURVEY_INSTANCE.ID.eq(instanceId))
                .execute();
    }


    public int updateDueDateForSurveyRun(long surveyRunId, LocalDate newDueDate) {
        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.DUE_DATE, toSqlDate(newDueDate))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .execute();
    }


    public int updateOwningRoleForSurveyRun(long surveyRunId, String role) {
        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.OWNING_ROLE, role)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .execute();
    }


    public int updateSubmitted(long instanceId, String userName) {
        checkNotNull(userName, "userName cannot be null");

        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.SUBMITTED_AT, Timestamp.valueOf(nowUtc()))
                .set(SURVEY_INSTANCE.SUBMITTED_BY, userName)
                .where(SURVEY_INSTANCE.ID.eq(instanceId))
                .execute();
    }


    public int markApproved(long instanceId, String userName) {
        checkNotNull(userName, "userName cannot be null");

        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.APPROVED_AT, Timestamp.valueOf(nowUtc()))
                .set(SURVEY_INSTANCE.APPROVED_BY, userName)
                .set(SURVEY_INSTANCE.STATUS, SurveyInstanceStatus.APPROVED.name())
                .where(SURVEY_INSTANCE.ID.eq(instanceId))
                .execute();
    }


    public void clearApproved(long instanceId) {
        dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.APPROVED_AT, (Timestamp) null)
                .set(SURVEY_INSTANCE.APPROVED_BY, (String) null)
                .where(SURVEY_INSTANCE.ID.eq(instanceId))
                .execute();
    }


    public List<SurveyInstance> findBySurveyInstanceIdSelector(Select<Record1<Long>> selector) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ID.in(selector))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findPreviousVersionsForInstance(long instanceId) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.eq(instanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public SurveyRunCompletionRate getCompletionRateForSurveyRun(Long surveyRunId) {
        Condition condition = SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId);
        return CollectionUtilities
                .maybeFirst(calcCompletionRateForSurveyRuns(condition))
                .orElse(null);
    }

    public List<SurveyRunCompletionRate> findCompletionRateForSurveyTemplate(Long surveyTemplateId) {
        Condition condition = SURVEY_INSTANCE.SURVEY_RUN_ID.in(DSL
                .select(SURVEY_RUN.ID)
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(surveyTemplateId)));

        return calcCompletionRateForSurveyRuns(condition);
    }


    private List<SurveyRunCompletionRate> calcCompletionRateForSurveyRuns(Condition surveyRunSelectionCondition) {
        Field<Integer> statCount = DSL.count(SURVEY_INSTANCE.ID).as("statCount");

        final Result<Record3<Long, String, Integer>> countsByRunAndStatus = dsl
                .select(SURVEY_INSTANCE.SURVEY_RUN_ID,
                        SURVEY_INSTANCE.STATUS,
                        statCount)
                .from(SURVEY_INSTANCE)
                .where(surveyRunSelectionCondition)
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .groupBy(SURVEY_INSTANCE.SURVEY_RUN_ID, SURVEY_INSTANCE.STATUS)
                .fetch();

        Map<Long, ImmutableSurveyRunCompletionRate.Builder> buildersByRunId = new HashMap<>();
        countsByRunAndStatus.forEach(r -> {
            Long runId = r.get(SURVEY_INSTANCE.SURVEY_RUN_ID);
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
        return r.get(SURVEY_INSTANCE.STATUS).equals(status.name());
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
}
