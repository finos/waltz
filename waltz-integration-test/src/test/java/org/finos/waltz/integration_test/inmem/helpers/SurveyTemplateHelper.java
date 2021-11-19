package org.finos.waltz.integration_test.inmem.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.ImmutableReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.survey.SurveyTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyTemplateHelper {


    @Autowired
    private SurveyTemplateService surveyTemplateService;

    @Autowired
    private SurveyQuestionService surveyQuestionService;


    public long createTemplate(String userId, String templateName) {
        SurveyTemplateChangeCommand cmd = ImmutableSurveyTemplateChangeCommand.builder()
                .name(templateName)
                .externalId("extId")
                .description("desc")
                .targetEntityKind(EntityKind.APPLICATION)
                .build();

        return surveyTemplateService.create(userId, cmd);
    }


    public void updateStatus(String userId, long templateId, ReleaseLifecycleStatus newStatus) {
        surveyTemplateService.updateStatus(
                userId,
                templateId,
                ImmutableReleaseLifecycleStatusChangeCommand.builder()
                        .newStatus(newStatus)
                        .build());
    }

    public long addQuestion(long templateId) {

        SurveyQuestion question = ImmutableSurveyQuestion
                .builder()
                .fieldType(SurveyQuestionFieldType.TEXT)
                .sectionName("section")
                .questionText("question")
                .surveyTemplateId(templateId)
                .build();

        return surveyQuestionService.create(question);
    }
}
