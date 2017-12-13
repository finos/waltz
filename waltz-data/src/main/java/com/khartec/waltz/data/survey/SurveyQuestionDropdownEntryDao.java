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
