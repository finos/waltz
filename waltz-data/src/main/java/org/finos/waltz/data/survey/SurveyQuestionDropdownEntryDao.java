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

import org.finos.waltz.model.survey.ImmutableSurveyQuestionDropdownEntry;
import org.finos.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.finos.waltz.schema.tables.records.SurveyQuestionDropdownEntryRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.SurveyQuestionDropdownEntry.SURVEY_QUESTION_DROPDOWN_ENTRY;

@Repository
public class SurveyQuestionDropdownEntryDao {


    private static final RecordMapper<Record, SurveyQuestionDropdownEntry> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionDropdownEntryRecord record = r.into(SURVEY_QUESTION_DROPDOWN_ENTRY);
        return ImmutableSurveyQuestionDropdownEntry.builder()
                .id(record.getId())
                .questionId(record.getQuestionId())
                .value(record.getValue())
                .position(record.getPosition())
                .build();
    };


    private static final Function<SurveyQuestionDropdownEntry, SurveyQuestionDropdownEntryRecord> TO_RECORD_MAPPER = entry -> {
        SurveyQuestionDropdownEntryRecord record = new SurveyQuestionDropdownEntryRecord();
        record.setQuestionId(entry.questionId().get());
        record.setValue(entry.value());
        record.setPosition(entry.position());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyQuestionDropdownEntryDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<SurveyQuestionDropdownEntry> findForQuestion(long questionId) {
        return dsl
                .select(SURVEY_QUESTION_DROPDOWN_ENTRY.fields())
                .from(SURVEY_QUESTION_DROPDOWN_ENTRY)
                .where(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID.eq(questionId))
                .orderBy(SURVEY_QUESTION_DROPDOWN_ENTRY.POSITION.asc(), SURVEY_QUESTION_DROPDOWN_ENTRY.VALUE.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public void saveEntries(long questionId, Collection<SurveyQuestionDropdownEntry> entries) {
        checkNotNull(entries, "entries cannot be null");

        dsl.transaction(config -> {
            DSLContext tx = DSL.using(config);

            tx.delete(SURVEY_QUESTION_DROPDOWN_ENTRY)
                    .where(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID.eq(questionId))
                    .execute();

            List<SurveyQuestionDropdownEntryRecord> records = entries
                    .stream()
                    .map(r -> ImmutableSurveyQuestionDropdownEntry.copyOf(r).withQuestionId(questionId))
                    .map(TO_RECORD_MAPPER)
                    .collect(toList());

            tx.batchInsert(records)
                    .execute();
        });
    }


    public List<SurveyQuestionDropdownEntry> findForSurveyInstance(long surveyInstanceId) {
        return dsl
                .select(SURVEY_QUESTION_DROPDOWN_ENTRY.fields())
                .from(SURVEY_QUESTION_DROPDOWN_ENTRY)
                .innerJoin(SURVEY_QUESTION).on(SURVEY_QUESTION.ID.eq(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_TEMPLATE.ID.eq(SURVEY_QUESTION.SURVEY_TEMPLATE_ID))
                .innerJoin(SURVEY_RUN).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID))
                .innerJoin(SURVEY_INSTANCE).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .where(SURVEY_INSTANCE.ID.eq(surveyInstanceId))
                .orderBy(SURVEY_QUESTION_DROPDOWN_ENTRY.POSITION.asc(), SURVEY_QUESTION_DROPDOWN_ENTRY.VALUE.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyQuestionDropdownEntry> findForSurveyTemplate(long surveyTemplateId) {
        return dsl
                .select(SURVEY_QUESTION_DROPDOWN_ENTRY.fields())
                .from(SURVEY_QUESTION_DROPDOWN_ENTRY)
                .innerJoin(SURVEY_QUESTION).on(SURVEY_QUESTION.ID.eq(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID))
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(surveyTemplateId))
                .orderBy(SURVEY_QUESTION_DROPDOWN_ENTRY.POSITION.asc(), SURVEY_QUESTION_DROPDOWN_ENTRY.VALUE.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int deleteForTemplate(Long templateId) {

        SelectConditionStep<Record1<Long>> questionIds = DSL
                .select(SURVEY_QUESTION.ID)
                .from(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(templateId));

        return dsl
                .deleteFrom(SURVEY_QUESTION_DROPDOWN_ENTRY)
                .where(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID.in(questionIds))
                .execute();
    }
}
