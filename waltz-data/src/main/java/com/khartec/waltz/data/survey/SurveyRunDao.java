/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.survey;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.schema.tables.records.SurveyRunRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.StringUtilities.join;
import static com.khartec.waltz.common.StringUtilities.splitThenMap;
import static com.khartec.waltz.schema.Tables.*;

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
                .issuanceKind(SurveyIssuanceKind.valueOf(record.getIssuanceKind()))
                .ownerId(record.getOwnerId())
                .contactEmail(record.getContactEmail())
                .status(SurveyRunStatus.valueOf(record.getStatus()))
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
        return dsl.select(SURVEY_RUN.fields())
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
        record.setDueDate(command.dueDate().map(Date::valueOf).orElse(null));
        record.setIssuanceKind(command.issuanceKind().name());
        record.setOwnerId(ownerId);
        record.setContactEmail(command.contactEmail().orElse(null));
        record.setStatus(SurveyRunStatus.DRAFT.name());

        record.store();
        return record.getId();
    }


    public int update(long surveyRunId, SurveyRunChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        return dsl.update(SURVEY_RUN)
                .set(SURVEY_RUN.NAME, command.name())
                .set(SURVEY_RUN.DESCRIPTION, command.description())
                .set(SURVEY_RUN.SELECTOR_ENTITY_KIND, command.selectionOptions().entityReference().kind().name())
                .set(SURVEY_RUN.SELECTOR_ENTITY_ID, command.selectionOptions().entityReference().id())
                .set(SURVEY_RUN.SELECTOR_HIERARCHY_SCOPE, command.selectionOptions().scope().name())
                .set(SURVEY_RUN.INVOLVEMENT_KIND_IDS, StringUtilities.join(command.involvementKindIds(), ID_SEPARATOR))
                .set(SURVEY_RUN.DUE_DATE, command.dueDate().map(Date::valueOf).orElse(null))
                .set(SURVEY_RUN.ISSUANCE_KIND, command.issuanceKind().name())
                .set(SURVEY_RUN.CONTACT_EMAIL, command.contactEmail().orElse(null))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int updateStatus(long surveyRunId, SurveyRunStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return dsl.update(SURVEY_RUN)
                .set(SURVEY_RUN.STATUS, newStatus.name())
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int updateDueDate(long surveyRunId, LocalDate newDueDate) {
        return dsl.update(SURVEY_RUN)
                .set(SURVEY_RUN.DUE_DATE, toSqlDate(newDueDate))
                .where(SURVEY_RUN.ID.eq(surveyRunId))
                .execute();
    }


    public int issue(long surveyRunId) {
        return dsl.update(SURVEY_RUN)
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
                .selectFrom(SURVEY_RUN)
                .where(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(templateId))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
