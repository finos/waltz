package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.survey.SurveyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
