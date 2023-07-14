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

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.ImmutableSurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.finos.waltz.schema.tables.records.SurveyQuestionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE;
import static org.finos.waltz.schema.Tables.SURVEY_QUESTION_RESPONSE;
import static org.finos.waltz.schema.Tables.SURVEY_RUN;
import static org.finos.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;

@Repository
public class SurveyQuestionDao {

    private static final RecordMapper<Record, SurveyQuestion> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionRecord record = r.into(SURVEY_QUESTION);

        Optional<EntityReference> qualifierRef = Optional
                .ofNullable(record.getEntityQualifierKind())
                .map(k -> mkRef(EntityKind.valueOf(k), record.getEntityQualifierId()));

        return ImmutableSurveyQuestion.builder()
                .id(record.getId())
                .surveyTemplateId(record.getSurveyTemplateId())
                .questionText(record.getQuestionText())
                .helpText(Optional.ofNullable(record.getHelpText()))
                .fieldType(SurveyQuestionFieldType.valueOf(record.getFieldType()))
                .sectionName(Optional.ofNullable(record.getSectionName()))
                .position(record.getPosition())
                .isMandatory(record.getIsMandatory())
                .allowComment(record.getAllowComment())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .inclusionPredicate(Optional.ofNullable(record.getInclusionPredicate()))
                .qualifierEntity(qualifierRef)
                .label(Optional.ofNullable(record.getLabel()))
                .parentExternalId(Optional.ofNullable(record.getParentExternalId()))
                .build();
    };


    private static final Function<SurveyQuestion, SurveyQuestionRecord> TO_RECORD_MAPPER = question -> {
        SurveyQuestionRecord record = new SurveyQuestionRecord();

        record.setSurveyTemplateId(question.surveyTemplateId());
        record.setQuestionText(question.questionText());
        record.setHelpText(question.helpText().orElse(""));
        record.setFieldType(question.fieldType().name());
        record.setSectionName(question.sectionName().orElse(""));
        record.setPosition(question.position());
        record.setIsMandatory(question.isMandatory());
        record.setAllowComment(question.allowComment());
        record.setExternalId(question.externalId()
                .filter(StringUtilities::notEmpty)
                .orElse(null));
        record.setParentExternalId(question.parentExternalId()
                .filter(StringUtilities::notEmpty)
                .orElse(null));
        record.setLabel(question.label().orElse(null));
        record.setInclusionPredicate(question.inclusionPredicate().orElse(null));

        question.qualifierEntity().ifPresent(ref -> {
            record.setEntityQualifierKind(ref.kind().name());
            record.setEntityQualifierId(ref.id());
        });

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyQuestionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<SurveyQuestion> findForTemplate(long templateId) {
        return findForTemplateIdSelector(DSL.select(DSL.val(templateId)));
    }


    public List<SurveyQuestion> findForSurveyRun(long surveyRunId) {
        return findForTemplateIdSelector(DSL
                .select(SURVEY_RUN.SURVEY_TEMPLATE_ID)
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.ID.eq(surveyRunId)));
    }


    public List<SurveyQuestion> findForSurveyInstance(long surveyInstanceId) {
        return findForTemplateIdSelector(DSL
                .select(SURVEY_RUN.SURVEY_TEMPLATE_ID)
                .from(SURVEY_RUN)
                .innerJoin(SURVEY_INSTANCE).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .where(SURVEY_INSTANCE.ID.eq(surveyInstanceId)));
    }


    public long create(SurveyQuestion surveyQuestion) {
        checkNotNull(surveyQuestion, "surveyQuestion cannot be null");

        SurveyQuestionRecord record = TO_RECORD_MAPPER.apply(surveyQuestion);
        return dsl.insertInto(SURVEY_QUESTION)
                .set(record)
                .returning(SURVEY_QUESTION.ID)
                .fetchOne()
                .getId();
    }


    public int update(SurveyQuestion surveyQuestion) {
        checkNotNull(surveyQuestion, "surveyQuestion cannot be null");
        checkTrue(surveyQuestion.id().isPresent(), "question id cannot be null");

        SurveyQuestionRecord record = TO_RECORD_MAPPER.apply(surveyQuestion);
        return dsl.update(SURVEY_QUESTION)
                .set(record)
                .where(SURVEY_QUESTION.ID.eq(surveyQuestion.id().get()))
                .execute();
    }


    public int delete(long questionId) {
        return dsl.delete(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.ID.eq(questionId))
                .execute();
    }


    public boolean hasResponses(long questionId) {
        return dsl.fetchExists(DSL
                .select(SURVEY_QUESTION_RESPONSE.fields())
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(questionId))
        );
    }


    public int deleteForTemplate(Long templateId){
        return dsl
                .deleteFrom(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(templateId))
                .execute();
    }


    private List<SurveyQuestion> findForTemplateIdSelector(Select<Record1<Long>> templateIdSelector) {
        return dsl
                .select(SURVEY_QUESTION.fields())
                .from(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.in(templateIdSelector))
                .orderBy(SURVEY_QUESTION.POSITION.asc(), SURVEY_QUESTION.QUESTION_TEXT)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Set<SurveyQuestion> findForIds(Set<Long> surveyQuestionsIds) {
        return dsl
                .select(SURVEY_QUESTION.fields())
                .from(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.ID.in(surveyQuestionsIds))
                .fetchSet(TO_DOMAIN_MAPPER);
    }
}
