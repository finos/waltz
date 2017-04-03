package com.khartec.waltz.service.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;

@Service
public class SurveyInstanceService {

    private final ChangeLogService changeLogService;
    private final PersonDao personDao;
    private final SurveyInstanceDao surveyInstanceDao;
    private final SurveyInstanceRecipientDao surveyInstanceRecipientDao;
    private final SurveyQuestionResponseDao surveyQuestionResponseDao;

    private final SurveyInstanceIdSelectorFactory surveyInstanceIdSelectorFactory;

    @Autowired
    public SurveyInstanceService(ChangeLogService changeLogService,
                                 PersonDao personDao,
                                 SurveyInstanceDao surveyInstanceDao,
                                 SurveyInstanceRecipientDao surveyInstanceRecipientDao,
                                 SurveyQuestionResponseDao surveyQuestionResponseDao,
                                 SurveyInstanceIdSelectorFactory surveyInstanceIdSelectorFactory) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyInstanceDao, "surveyInstanceDao cannot be null");
        checkNotNull(surveyInstanceRecipientDao, "surveyInstanceRecipientDao cannot be null");
        checkNotNull(surveyQuestionResponseDao, "surveyQuestionResponseDao cannot be null");
        checkNotNull(surveyInstanceIdSelectorFactory, "surveyInstanceIdSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.personDao = personDao;
        this.surveyInstanceDao = surveyInstanceDao;
        this.surveyInstanceRecipientDao = surveyInstanceRecipientDao;
        this.surveyQuestionResponseDao = surveyQuestionResponseDao;
        this.surveyInstanceIdSelectorFactory = surveyInstanceIdSelectorFactory;
    }


    public SurveyInstance getById(long instanceId) {
        return surveyInstanceDao.getById(instanceId);
    }


    public List<SurveyInstance> findForRecipient(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person person = personDao.getByUserName(userName);
        checkNotNull(person, "userName " + userName + " cannot be resolved");

        return surveyInstanceDao.findForRecipient(person.id().get());
    }


    public List<SurveyInstance> findForSurveyRun(long surveyRunId) {
        return surveyInstanceDao.findForSurveyRun(surveyRunId);
    }


    public List<SurveyInstanceQuestionResponse> findResponses(long instanceId) {
        return surveyQuestionResponseDao.findForInstance(instanceId);
    }


    public List<SurveyInstanceRecipient> findRecipients(long instanceId) {
        return surveyInstanceRecipientDao.findForSurveyInstance(instanceId);
    }


    public boolean saveResponse(String userName,
                                long instanceId,
                                SurveyQuestionResponse questionResponse) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(questionResponse, "questionResponse cannot be null");

        Person person = personDao.getByUserName(userName);
        checkNotNull(person, "userName " + userName + " cannot be resolved");

        boolean isPersonInstanceRecipient = surveyInstanceRecipientDao.isPersonInstanceRecipient(
                person.id().get(),
                instanceId);
        checkTrue(isPersonInstanceRecipient, "Permission denied");

        SurveyInstance surveyInstance = surveyInstanceDao.getById(instanceId);
        checkTrue(surveyInstance.status() == SurveyInstanceStatus.NOT_STARTED
                    || surveyInstance.status() == SurveyInstanceStatus.IN_PROGRESS,
                "Survey instance cannot be updated, current status: " + surveyInstance.status());

        SurveyInstanceQuestionResponse instanceQuestionResponse = ImmutableSurveyInstanceQuestionResponse.builder()
                .surveyInstanceId(instanceId)
                .personId(person.id().get())
                .lastUpdatedAt(DateTimeUtilities.nowUtc())
                .questionResponse(questionResponse)
                .build();

        surveyQuestionResponseDao.saveResponse(instanceQuestionResponse);

        return true;
    }


    public int updateStatus(String userName, long instanceId, SurveyInstanceStatusChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        int result = surveyInstanceDao.updateStatus(instanceId, command.newStatus());

        if (result > 0) {
            if (command.newStatus() == SurveyInstanceStatus.COMPLETED) {
                surveyInstanceDao.updateSubmitted(instanceId, userName);
            }

            changeLogService.write(
                    ImmutableChangeLog.builder()
                            .operation(Operation.ADD)
                            .userId(userName)
                            .parentReference(EntityReference.mkRef(EntityKind.SURVEY_INSTANCE, instanceId))
                            .message("Survey Instance: status changed to " + command.newStatus())
                            .build());
        }

        return result;
    }


    public List<SurveyInstance> findBySurveyInstanceIdSelector(IdSelectionOptions idSelectionOptions) {
        checkNotNull(idSelectionOptions,  "idSelectionOptions cannot be null");

        Select<Record1<Long>> selector = surveyInstanceIdSelectorFactory.apply(idSelectionOptions);

        return surveyInstanceDao.findBySurveyInstanceIdSelector(selector);
    }


    public long addRecipient(SurveyInstanceRecipientCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        return surveyInstanceRecipientDao.create(command);
    }


    public boolean updateRecipient(SurveyInstanceRecipientUpdateCommand command) {
        checkNotNull(command, "command cannot be null");

        return surveyInstanceRecipientDao.update(command);
    }


    public boolean delete(long surveyInstanceRecipientId) {
        return surveyInstanceRecipientDao.delete(surveyInstanceRecipientId);
    }

}
