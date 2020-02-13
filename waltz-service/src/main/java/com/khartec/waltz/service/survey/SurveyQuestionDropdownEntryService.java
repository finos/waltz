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

package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionDropdownEntry;
import com.khartec.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;

@Service
public class SurveyQuestionDropdownEntryService {

    private final SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao;


    @Autowired
    public SurveyQuestionDropdownEntryService(SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao) {
        checkNotNull(surveyQuestionDropdownEntryDao, "surveyQuestionDropdownEntryDao cannot be null");

        this.surveyQuestionDropdownEntryDao = surveyQuestionDropdownEntryDao;
    }


    public List<SurveyQuestionDropdownEntry> findForQuestion(long questionId) {
        return surveyQuestionDropdownEntryDao.findForQuestion(questionId);
    }


    public boolean saveEntries(long questionId, List<SurveyQuestionDropdownEntry> entries) {
        checkNotNull(entries, "entries cannot be null");

        AtomicInteger counter = new AtomicInteger(0);
        List<SurveyQuestionDropdownEntry> sanitisedEntries = entries.stream()
                .map(e -> ImmutableSurveyQuestionDropdownEntry.copyOf(e)
                        .withQuestionId(questionId)
                        .withPosition(counter.incrementAndGet()))
                .collect(toList());

        surveyQuestionDropdownEntryDao.saveEntries(questionId, sanitisedEntries);

        return true;
    }
}
