package com.khartec.waltz.service.survey;


import com.khartec.waltz.data.survey.SurveyQuestionDao;
import com.khartec.waltz.model.survey.SurveyQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;

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


    public List<SurveyQuestion> findForSurveyRun(long surveyRunId) {
        return surveyQuestionDao.findForSurveyRun(surveyRunId);
    }


    public List<SurveyQuestion> findForSurveyInstance(long surveyInstanceId) {
        return surveyQuestionDao.findForSurveyInstance(surveyInstanceId);
    }


    public long create(SurveyQuestion surveyQuestion) {
        checkNotNull(surveyQuestion, "surveyQuestion cannot be null");

        return surveyQuestionDao.create(surveyQuestion);
    }


    public int update(SurveyQuestion surveyQuestion) {
        checkNotNull(surveyQuestion, "surveyQuestion cannot be null");
        checkTrue(surveyQuestion.id().isPresent(), "question id cannot be null");

        return surveyQuestionDao.update(surveyQuestion);
    }


    public int delete(long questionId) {
        return surveyQuestionDao.delete(questionId);
    }
}
