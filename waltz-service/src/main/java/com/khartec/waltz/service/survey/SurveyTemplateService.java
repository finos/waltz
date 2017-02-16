package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.survey.SurveyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class SurveyTemplateService {
    private final SurveyTemplateDao surveyTemplateDao;

    @Autowired
    public SurveyTemplateService(SurveyTemplateDao surveyTemplateDao) {
        this.surveyTemplateDao = surveyTemplateDao;
    }


    public SurveyTemplate getById(long id) {
        return surveyTemplateDao.getById(id);
    }


    public List<SurveyTemplate> findAllActive() {
        return surveyTemplateDao.findAllActive();
    }


    public long create(SurveyTemplate surveyTemplate) {
        checkNotNull(surveyTemplate, "surveyTemplate cannot be null");

        return surveyTemplateDao.create(surveyTemplate);
    }
}
