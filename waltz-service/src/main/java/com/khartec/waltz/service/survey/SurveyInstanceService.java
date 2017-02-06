package com.khartec.waltz.service.survey;


import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;

@Service
public class SurveyInstanceService {

    private final PersonDao personDao;
    private final SurveyInstanceDao surveyInstanceDao;
    private final SurveyInstanceRecipientDao surveyInstanceRecipientDao;
    private final SurveyQuestionResponseDao surveyQuestionResponseDao;


    @Autowired
    public SurveyInstanceService(PersonDao personDao,
                                 SurveyInstanceDao surveyInstanceDao,
                                 SurveyInstanceRecipientDao surveyInstanceRecipientDao,
                                 SurveyQuestionResponseDao surveyQuestionResponseDao) {
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyInstanceDao, "surveyInstanceDao cannot be null");
        checkNotNull(surveyInstanceRecipientDao, "surveyInstanceRecipientDao cannot be null");
        checkNotNull(surveyQuestionResponseDao, "surveyQuestionResponseDao cannot be null");

        this.personDao = personDao;
        this.surveyInstanceDao = surveyInstanceDao;
        this.surveyInstanceRecipientDao = surveyInstanceRecipientDao;
        this.surveyQuestionResponseDao = surveyQuestionResponseDao;
    }


    public SurveyInstance getById(long instanceId) {
        return surveyInstanceDao.getById(instanceId);
    }


    public List<SurveyQuestionResponse> findResponses(long instanceId) {
        return surveyQuestionResponseDao.findForInstance(instanceId);
    }


    public List<SurveyQuestionResponse> saveResponses(String userName,
                                                      long instanceId,
                                                      SurveyInstanceResponseCommand command) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

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

        Map<Boolean, List<SurveyQuestionResponseChange>> updatesInserts = command.questionResponseChanges()
                .stream()
                .collect(Collectors.partitioningBy(change -> change.id().isPresent()));

        List<SurveyQuestionResponseChange> updates = updatesInserts.get(true);
        List<SurveyQuestionResponseChange> inserts = updatesInserts.get(false);

        surveyQuestionResponseDao.updateQuestionResponses(person.id().get(), instanceId, updates);
        surveyQuestionResponseDao.insertQuestionResponses(person.id().get(), instanceId, inserts);

        return findResponses(instanceId);
    }


    public int updateStatus(long instanceId, SurveyInstanceStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return surveyInstanceDao.updateStatus(instanceId, newStatus);
    }
}
