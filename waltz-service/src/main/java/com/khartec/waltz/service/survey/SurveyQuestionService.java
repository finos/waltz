package com.khartec.waltz.service.survey;


import com.khartec.waltz.data.survey.SurveyQuestionDao;
import com.khartec.waltz.model.survey.SurveyQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class SurveyQuestionService {

    private final SurveyQuestionDao surveyQuestionDao;


    @Autowired
    public SurveyQuestionService(SurveyQuestionDao surveyQuestionDao) {
        checkNotNull(surveyQuestionDao, "surveyQuestionDao cannot be null");

        this.surveyQuestionDao = surveyQuestionDao;
    }


    public List<SurveyQuestion> findForSurveyTemplate(long templateId) {
        return surveyQuestionDao.findForTemplate(templateId);
    }


    public List<SurveyQuestion> findForSurveyInstance(long surveyInstanceId) {
        return surveyQuestionDao.findForSurveyInstance(surveyInstanceId);
    }
}
