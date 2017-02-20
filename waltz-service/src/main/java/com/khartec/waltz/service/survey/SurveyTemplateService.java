package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class SurveyTemplateService {
    private final ChangeLogService changeLogService;

    private final SurveyTemplateDao surveyTemplateDao;

    @Autowired
    public SurveyTemplateService(ChangeLogService changeLogService,
                                 SurveyTemplateDao surveyTemplateDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");

        this.changeLogService = changeLogService;
        this.surveyTemplateDao = surveyTemplateDao;
    }


    public SurveyTemplate getById(long id) {
        return surveyTemplateDao.getById(id);
    }


    public List<SurveyTemplate> findAllActive() {
        return surveyTemplateDao.findAllActive();
    }


    public long create(String userName, SurveyTemplate surveyTemplate) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(surveyTemplate, "surveyTemplate cannot be null");

        long surveyTemplateId = surveyTemplateDao.create(surveyTemplate);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, surveyTemplateId))
                        .message("Survey Template: '" + surveyTemplate.name() + "' added")
                        .build());

        return surveyTemplateId;
    }
}
