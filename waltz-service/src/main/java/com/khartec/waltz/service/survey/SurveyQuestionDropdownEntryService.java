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

package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionDropdownEntry;
import com.khartec.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

        List<SurveyQuestionDropdownEntry> sanitisedEntries = entries.stream()
                .map(e -> ImmutableSurveyQuestionDropdownEntry.copyOf(e)
                        .withQuestionId(questionId))
                .collect(toList());

        surveyQuestionDropdownEntryDao.saveEntries(questionId, sanitisedEntries);

        return true;
    }
}
