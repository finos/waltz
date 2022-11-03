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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.schema.tables.records.SurveyRunRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.common.StringUtilities.splitThenMap;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class SurveyRunDao {

    private static final String ID_SEPARATOR = ";";

    private static final RecordMapper<Record, SurveyRun> TO_DOMAIN_MAPPER = r -> {
        SurveyRunRecord record = r.into(SURVEY_RUN);
        return ImmutableSurveyRun.builder()
                .id(record.getId())
                .surveyTemplateId(record.getSurveyTemplateId())
                .name(record.getName())
                .description(record.getDescription())
                .selectionOptions(IdSelectionOptions.mkOpts(
                        EntityReference.mkRef(
                                EntityKind.valueOf(record.getSelectorEntityKind()),
                                record.getSelectorEntityId()),
                        HierarchyQueryScope.valueOf(record.getSelectorHierarchyScope())))
                .involvementKindIds(splitThenMap(
                        record.getInvolvementKindIds(),
                        ID_SEPARATOR,
                        Long::valueOf))
                .issuedOn(Optional.ofNullable(record.getIssuedOn()).map(Date::toLocalDate))
                .dueDate(record.getDueDate().toLocalDate())
                .approvalDueDate(toLocalDate(record.getApprovalDueDate()))
                .issuanceKind(SurveyIssuanceKind.valueOf(record.getIssuanceKind()))
                .ownerId(record.getOwnerId())
                .contactEmail(record.getContactEmail())
                .status(SurveyRunStatus.valueOf(record.getStatus()))
                .ownerInvKindIds(splitThenMap(
                        record.getOwnerInvKindIds(),
                        ID_SEPARATOR,
                        Long::valueOf))
                .isDefault(record.getIsDefault())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyRunDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyRun getById(long id) {
        return dsl.select(SURVEY_RUN.fields())
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SurveyRun> findForRecipient(long personId) {
        return dsl
                .select(SURVEY_RUN.fields())
                .from(SURVEY_RUN)
                .innerJoin(SURVEY_INSTANCE)
                .on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .where(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(long ownerId, SurveyRunCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyRunRecord record = dsl.newRecord(SURVEY_RUN);
        record.setSurveyTemplateId(command.surveyTemplateId());
        record.setName(command.name());
        record.setDescription(command.description());
        record.setSelectorEntityKind(command.selectionOptions().entityReference().kind().name());
        record.setSelectorEntityId(command.selectionOptions().entityReference().id());
        record.setSelectorHierarchyScope(command.selectionOptions().scope().name());
        record.setInvolvementKindIds(join(command.involvementKindIds(), ID_SEPARATOR));
        record.setDueDate(toSqlDate(command.dueDate()));
        record.setApprovalDueDate(toSqlDate(command.approvalDueDate()));
        record.setIssuanceKind(command.issuanceKind().name());
        record.setOwnerId(ownerId);
        record.setContactEmail(command.contactEmail());
        record.setStatus(SurveyRunStatus.DRAFT.name());
        record.setOwnerInvKindIds(join(command.ownerInvKindIds(), ID_SEPARATOR));

        record.store();
        return record.getId();
    }


    public int delete(long surveyRunId) {
        return dsl
                .delete(SURVEY_RUN)
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int update(long surveyRunId, SurveyRunChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        return dsl
                .update(SURVEY_RUN)
                .set(SURVEY_RUN.NAME, command.name())
                .set(SURVEY_RUN.DESCRIPTION, command.description())
                .set(SURVEY_RUN.SELECTOR_ENTITY_KIND, command.selectionOptions().entityReference().kind().name())
                .set(SURVEY_RUN.SELECTOR_ENTITY_ID, command.selectionOptions().entityReference().id())
                .set(SURVEY_RUN.SELECTOR_HIERARCHY_SCOPE, command.selectionOptions().scope().name())
                .set(SURVEY_RUN.INVOLVEMENT_KIND_IDS, StringUtilities.join(command.involvementKindIds(), ID_SEPARATOR))
                .set(SURVEY_RUN.DUE_DATE, command.dueDate().map(Date::valueOf).orElse(null))
                .set(SURVEY_RUN.APPROVAL_DUE_DATE, command.approvalDueDate().map(Date::valueOf).orElse(null))
                .set(SURVEY_RUN.ISSUANCE_KIND, command.issuanceKind().name())
                .set(SURVEY_RUN.CONTACT_EMAIL, command.contactEmail().orElse(null))
                .set(SURVEY_RUN.OWNER_INV_KIND_IDS, join(command.ownerInvKindIds(), ID_SEPARATOR))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int updateStatus(long surveyRunId, SurveyRunStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return dsl
                .update(SURVEY_RUN)
                .set(SURVEY_RUN.STATUS, newStatus.name())
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int updateDueDate(long surveyRunId, LocalDate newDueDate) {
        return dsl
                .update(SURVEY_RUN)
                .set(SURVEY_RUN.DUE_DATE, toSqlDate(newDueDate))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int updateApprovalDueDate(long surveyRunId, LocalDate newDueDate) {
        return dsl
                .update(SURVEY_RUN)
                .set(SURVEY_RUN.APPROVAL_DUE_DATE, toSqlDate(newDueDate))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int issue(long surveyRunId) {
        return dsl
                .update(SURVEY_RUN)
                .set(SURVEY_RUN.STATUS, SurveyRunStatus.ISSUED.name())
                .set(SURVEY_RUN.ISSUED_ON, java.sql.Date.valueOf(DateTimeUtilities.nowUtc().toLocalDate()))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public List<SurveyRun> findBySurveyInstanceIdSelector(Select<Record1<Long>> idSelector) {
        return dsl
                .selectDistinct(SURVEY_RUN.fields())
                .from(SURVEY_RUN)
                .join(SURVEY_INSTANCE).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .where(SURVEY_INSTANCE.ID.in(idSelector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyRun> findByTemplateId(long templateId) {
        return dsl
                .select(SURVEY_RUN.fields())
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(templateId))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
