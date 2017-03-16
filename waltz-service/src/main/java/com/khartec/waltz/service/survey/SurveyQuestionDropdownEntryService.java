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
