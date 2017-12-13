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

import com.khartec.waltz.model.survey.ImmutableSurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionFieldType;
import com.khartec.waltz.schema.tables.records.SurveyQuestionRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_RUN;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;

@Repository
public class SurveyQuestionDao {

    private static final RecordMapper<Record, SurveyQuestion> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionRecord record = r.into(SURVEY_QUESTION);
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


    private List<SurveyQuestion> findForTemplateIdSelector(Select<Record1<Long>> templateIdSelector) {
        return dsl.selectFrom(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.in(templateIdSelector))
                .orderBy(SURVEY_QUESTION.POSITION.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
