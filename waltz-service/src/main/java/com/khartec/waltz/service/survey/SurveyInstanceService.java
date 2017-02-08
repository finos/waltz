package com.khartec.waltz.service.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


    public List<SurveyInstance> findForRecipient(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person person = personDao.getByUserName(userName);
        checkNotNull(person, "userName " + userName + " cannot be resolved");

        return surveyInstanceDao.findForRecipient(person.id().get());
    }


    public List<SurveyInstanceQuestionResponse> findResponses(long instanceId) {
        return surveyQuestionResponseDao.findForInstance(instanceId);
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


    public int updateStatus(long instanceId, SurveyInstanceStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return surveyInstanceDao.updateStatus(instanceId, newStatus);
    }
}
