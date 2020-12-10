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

import com.khartec.waltz.model.survey.ImmutableSurveyQuestionDropdownEntry;
import com.khartec.waltz.model.survey.SurveyQuestionDropdownEntry;
import com.khartec.waltz.schema.tables.records.SurveyQuestionDropdownEntryRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SurveyQuestionDropdownEntry.SURVEY_QUESTION_DROPDOWN_ENTRY;
import static java.util.stream.Collectors.toList;

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
        return dsl.select(SURVEY_QUESTION_DROPDOWN_ENTRY.fields())
                .from(SURVEY_QUESTION_DROPDOWN_ENTRY)
                .where(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID.eq(questionId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public void saveEntries(long questionId, List<SurveyQuestionDropdownEntry> entries) {
        checkNotNull(entries, "entries cannot be null");

        dsl.transaction(config -> {
            DSLContext tx = DSL.using(config);

            tx.delete(SURVEY_QUESTION_DROPDOWN_ENTRY)
                    .where(SURVEY_QUESTION_DROPDOWN_ENTRY.QUESTION_ID.eq(questionId))
                    .execute();

            List<SurveyQuestionDropdownEntryRecord> records = entries.stream()
                    .map(TO_RECORD_MAPPER)
                    .collect(toList());

            tx.batchInsert(records)
                    .execute();
        });
    }
}
