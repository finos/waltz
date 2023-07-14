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

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.survey.ImmutableSurveyInstance;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceInfo;
import org.finos.waltz.model.survey.ImmutableSurveyRun;
import org.finos.waltz.model.survey.SurveyInstanceInfo;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.model.survey.SurveyIssuanceKind;
import org.finos.waltz.model.survey.SurveyRunStatus;
import org.finos.waltz.schema.tables.records.SurveyInstanceRecord;
import org.finos.waltz.schema.tables.records.SurveyRunRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.data.JooqUtilities.maybeReadRef;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;

@Repository
public class SurveyViewDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                SURVEY_INSTANCE.ENTITY_ID,
                SURVEY_INSTANCE.ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE))
            .as("entity_name");

    private static final Field<String> QUALIFIER_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                SURVEY_INSTANCE.ENTITY_QUALIFIER_ID,
                SURVEY_INSTANCE.ENTITY_QUALIFIER_KIND,
                newArrayList(EntityKind.MEASURABLE))
            .as("qualifier_entity_name");

    private static final Field<String> EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
                    SURVEY_INSTANCE.ENTITY_ID,
                    SURVEY_INSTANCE.ENTITY_KIND,
                    newArrayList(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE))
            .as("external_id");

    private static final String ID_SEPARATOR = ";";

    private static final Condition IS_ORIGINAL_INSTANCE_CONDITION = SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull();

    private static SurveyInstanceInfo mkSurveyInstanceInfo(Record r, Map<Long, List<Long>> surveyInvolvementGroupKindIds) {

        SurveyInstanceRecord instanceRecord = r.into(SURVEY_INSTANCE);
        ImmutableSurveyInstance surveyInstance = ImmutableSurveyInstance.builder()
                .id(instanceRecord.getId())
                .surveyRunId(instanceRecord.getSurveyRunId())
                .surveyEntity(mkRef(
                        EntityKind.valueOf(instanceRecord.getEntityKind()),
                        instanceRecord.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .surveyEntityExternalId(r.getValue(EXTERNAL_ID_FIELD))
                .status(SurveyInstanceStatus.valueOf(instanceRecord.getStatus()))
                .dueDate(instanceRecord.getDueDate().toLocalDate())
                .approvalDueDate(instanceRecord.getApprovalDueDate().toLocalDate())
                .submittedAt(ofNullable(instanceRecord.getSubmittedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .submittedBy(instanceRecord.getSubmittedBy())
                .approvedAt(ofNullable(instanceRecord.getApprovedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .approvedBy(instanceRecord.getApprovedBy())
                .originalInstanceId(instanceRecord.getOriginalInstanceId())
                .owningRole(instanceRecord.getOwningRole())
                .name(instanceRecord.getName())
                .qualifierEntity(maybeReadRef(
                        r,
                        SURVEY_INSTANCE.ENTITY_QUALIFIER_KIND,
                        SURVEY_INSTANCE.ENTITY_QUALIFIER_ID,
                        QUALIFIER_NAME_FIELD)
                        .orElse(null))
                .issuedOn(toLocalDate(instanceRecord.getIssuedOn()))
                .build();

        SurveyRunRecord runRecord = r.into(SURVEY_RUN);

        Long recipientInvolvementGroupId = runRecord.getRecipientInvolvementGroupId();
        Long ownerInvolvementGroupId = runRecord.getOwnerInvolvementGroupId();

        List<Long> recipients = surveyInvolvementGroupKindIds.getOrDefault(recipientInvolvementGroupId, emptyList());
        List<Long> owners = surveyInvolvementGroupKindIds.getOrDefault(ownerInvolvementGroupId, emptyList());

        ImmutableSurveyRun run = ImmutableSurveyRun.builder()
                .id(runRecord.getId())
                .surveyTemplateId(runRecord.getSurveyTemplateId())
                .name(runRecord.getName())
                .description(runRecord.getDescription())
                .selectionOptions(IdSelectionOptions.mkOpts(
                        mkRef(
                                EntityKind.valueOf(runRecord.getSelectorEntityKind()),
                                runRecord.getSelectorEntityId()),
                        HierarchyQueryScope.valueOf(runRecord.getSelectorHierarchyScope())))
                .involvementKindIds(fromCollection(recipients))
                .ownerInvKindIds(fromCollection(owners))
                .issuedOn(ofNullable(runRecord.getIssuedOn()).map(Date::toLocalDate))
                .dueDate(runRecord.getDueDate().toLocalDate())
                .approvalDueDate(runRecord.getApprovalDueDate().toLocalDate())
                .issuanceKind(SurveyIssuanceKind.valueOf(runRecord.getIssuanceKind()))
                .ownerId(runRecord.getOwnerId())
                .contactEmail(runRecord.getContactEmail())
                .status(SurveyRunStatus.valueOf(runRecord.getStatus()))
                .build();

        return ImmutableSurveyInstanceInfo.builder()
                .surveyInstance(surveyInstance)
                .surveyRun(run)
                .surveyTemplateRef(mkRef(
                        EntityKind.SURVEY_TEMPLATE,
                        r.get(SURVEY_TEMPLATE.ID),
                        r.get(SURVEY_TEMPLATE.NAME),
                        r.get(SURVEY_TEMPLATE.DESCRIPTION),
                        r.get(SURVEY_TEMPLATE.EXTERNAL_ID)))
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public SurveyViewDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyInstanceInfo getById(long instanceId) {

        Map<Long, List<Long>> surveyInvolvementGroupKindIds = findSurveyInvolvementGroupKindIds();

        return dsl
                .select(SURVEY_INSTANCE.fields())
                .select(SURVEY_RUN.fields())
                .select(SURVEY_TEMPLATE.NAME,
                        SURVEY_TEMPLATE.ID,
                        SURVEY_TEMPLATE.DESCRIPTION,
                        SURVEY_TEMPLATE.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(QUALIFIER_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .where(SURVEY_INSTANCE.ID.eq(instanceId))
                .fetchOne(r -> mkSurveyInstanceInfo(r, surveyInvolvementGroupKindIds));
    }


    public Set<SurveyInstanceInfo> findForRecipient(long personId) {

        Map<Long, List<Long>> surveyInvolvementGroupKindIds = findSurveyInvolvementGroupKindIds();

        return dsl
                .select(SURVEY_INSTANCE.fields())
                .select(SURVEY_RUN.fields())
                .select(SURVEY_TEMPLATE.NAME,
                        SURVEY_TEMPLATE.ID,
                        SURVEY_TEMPLATE.DESCRIPTION,
                        SURVEY_TEMPLATE.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(QUALIFIER_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .where(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId))
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .and(SURVEY_INSTANCE.STATUS.ne(SurveyInstanceStatus.WITHDRAWN.name()))
                .and(SURVEY_TEMPLATE.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()))
                .fetchSet(r -> mkSurveyInstanceInfo(r, surveyInvolvementGroupKindIds));
    }



    public Set<SurveyInstanceInfo> findForOwner(Long personId) {

        Condition isRunOwnerOrHasOwnerInvolvement = SURVEY_INSTANCE_OWNER.PERSON_ID.eq(personId).or(SURVEY_RUN.OWNER_ID.eq(personId));

        Map<Long, List<Long>> surveyInvolvementGroupKindIds = findSurveyInvolvementGroupKindIds();

        SelectConditionStep<Record> selectSurveysByOwningInvolvement = dsl
                .select(SURVEY_INSTANCE.fields())
                .select(SURVEY_RUN.fields())
                .select(SURVEY_TEMPLATE.NAME,
                        SURVEY_TEMPLATE.ID,
                        SURVEY_TEMPLATE.DESCRIPTION,
                        SURVEY_TEMPLATE.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(QUALIFIER_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_OWNER)
                .on(SURVEY_INSTANCE_OWNER.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .where(isRunOwnerOrHasOwnerInvolvement)
                .and(IS_ORIGINAL_INSTANCE_CONDITION)
                .and(SURVEY_INSTANCE.STATUS.ne(SurveyInstanceStatus.WITHDRAWN.name()))
                .and(SURVEY_TEMPLATE.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()));

        SelectConditionStep<Record> selectSurveysByOwningRole = dsl
                .select(SURVEY_INSTANCE.fields())
                .select(SURVEY_RUN.fields())
                .select(SURVEY_TEMPLATE.NAME,
                        SURVEY_TEMPLATE.ID,
                        SURVEY_TEMPLATE.DESCRIPTION,
                        SURVEY_TEMPLATE.EXTERNAL_ID)
                .select(ENTITY_NAME_FIELD)
                .select(QUALIFIER_NAME_FIELD)
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .where(IS_ORIGINAL_INSTANCE_CONDITION)
                .and(SURVEY_INSTANCE.STATUS.ne(SurveyInstanceStatus.WITHDRAWN.name()))
                .and(SURVEY_TEMPLATE.STATUS.eq(ReleaseLifecycleStatus.ACTIVE.name()))
                .and(SURVEY_INSTANCE.OWNING_ROLE.in(fetchUserRoles(personId)));

        return selectSurveysByOwningInvolvement
                .union(selectSurveysByOwningRole)
                .fetchSet(r -> mkSurveyInstanceInfo(r, surveyInvolvementGroupKindIds));
    }


    private Set<String> fetchUserRoles(Long personId) {
        return dsl
                .select(USER_ROLE.ROLE)
                .from(PERSON)
                .innerJoin(USER_ROLE).on(PERSON.EMAIL.eq(USER_ROLE.USER_NAME))
                .where(PERSON.ID.eq(personId))
                .fetchSet(r -> r.get(USER_ROLE.ROLE));
    }

    private Map<Long, List<Long>> findSurveyInvolvementGroupKindIds() {
        return dsl
                .select(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID,
                        INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(INVOLVEMENT_GROUP_ENTRY)
                .fetchGroups(
                        r -> r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID),
                        r -> r.get(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID));
    }
}
