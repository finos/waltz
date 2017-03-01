package com.khartec.waltz.service.survey;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.ImmutableSurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplateChangeCommand;
import com.khartec.waltz.model.survey.SurveyTemplateStatus;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;

@Service
public class SurveyTemplateService {
    private final ChangeLogService changeLogService;

    private final PersonDao personDao;
    private final SurveyTemplateDao surveyTemplateDao;

    @Autowired
    public SurveyTemplateService(ChangeLogService changeLogService,
                                 PersonDao personDao,
                                 SurveyTemplateDao surveyTemplateDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");

        this.changeLogService = changeLogService;
        this.personDao = personDao;
        this.surveyTemplateDao = surveyTemplateDao;
    }


    public SurveyTemplate getById(long id) {
        return surveyTemplateDao.getById(id);
    }


    public List<SurveyTemplate> findAllActive() {
        return surveyTemplateDao.findAllActive();
    }


    public long create(String userName, SurveyTemplateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        Person owner = personDao.getByUserName(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        long surveyTemplateId = surveyTemplateDao.create(ImmutableSurveyTemplate.builder()
                .name(command.name())
                .description(command.description())
                .targetEntityKind(command.targetEntityKind())
                .ownerId(owner.id().get())
                .createdAt(DateTimeUtilities.nowUtc())
                .status(SurveyTemplateStatus.DRAFT)
                .build());

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, surveyTemplateId))
                        .message("Survey Template: '" + command.name() + "' added")
                        .build());

        return surveyTemplateId;
    }


    public int update(String userName, SurveyTemplateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");
        checkTrue(command.id().isPresent(), "template id cannot be null");

        int numUpdated = surveyTemplateDao.update(command);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, command.id().get()))
                        .message("Survey Template: '" + command.name() + "' updated")
                        .build());

        return numUpdated;
    }
}
