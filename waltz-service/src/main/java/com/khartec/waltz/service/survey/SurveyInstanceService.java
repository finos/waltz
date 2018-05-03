/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static java.util.Optional.ofNullable;

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
                    || surveyInstance.status() == SurveyInstanceStatus.IN_PROGRESS
                    || surveyInstance.status() == SurveyInstanceStatus.REJECTED,
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

        SurveyInstance surveyInstance = surveyInstanceDao.getById(instanceId);

        // if survey is being sent back, store current responses as a version
        if ((surveyInstance.status() == SurveyInstanceStatus.COMPLETED || surveyInstance.status() == SurveyInstanceStatus.APPROVED)
                && command.newStatus() == SurveyInstanceStatus.REJECTED) {
            long versionedInstanceId = surveyInstanceDao.createPreviousVersion(surveyInstance);
            surveyQuestionResponseDao.cloneResponses(surveyInstance.id().get(), versionedInstanceId);
            surveyInstanceDao.clearApproved(instanceId);
        }

        int result = surveyInstanceDao.updateStatus(instanceId, command.newStatus());

        if (result > 0) {
            if (command.newStatus() == SurveyInstanceStatus.COMPLETED) {
                surveyInstanceDao.updateSubmitted(instanceId, userName);
            }

            changeLogService.write(
                    ImmutableChangeLog.builder()
                            .operation(Operation.UPDATE)
                            .userId(userName)
                            .parentReference(EntityReference.mkRef(EntityKind.SURVEY_INSTANCE, instanceId))
                            .message("Survey Instance: status changed to " + command.newStatus()
                                    + command.reason().map(r -> ", [Reason]: " + r).orElse(""))
                            .build());
        }

        return result;
    }


    public int updateDueDate(String userName, long instanceId, DateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        LocalDate newDueDate = command.newDateVal().orElse(null);

        checkNotNull(newDueDate, "newDueDate cannot be null");

        int result = surveyInstanceDao.updateDueDate(instanceId, newDueDate);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_INSTANCE, instanceId))
                        .message("Survey Instance: due date changed to " + newDueDate)
                        .build());

        return result;
    }


    public int markApproved(String userName, long instanceId, String reason) {
        checkNotNull(userName, "userName cannot be null");

        SurveyInstance surveyInstance = surveyInstanceDao.getById(instanceId);

        checkTrue(surveyInstance.status() == SurveyInstanceStatus.COMPLETED, "Only completed surveys can be approved");

        int result = surveyInstanceDao.markApproved(instanceId, userName);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_INSTANCE, instanceId))
                        .message("Survey Instance: Approved" + ofNullable(reason).map(r -> ", [Reason]: " + r).orElse(""))
                        .build());

        return result;
    }


    public List<SurveyInstance> findBySurveyInstanceIdSelector(IdSelectionOptions idSelectionOptions) {
        checkNotNull(idSelectionOptions,  "idSelectionOptions cannot be null");

        Select<Record1<Long>> selector = surveyInstanceIdSelectorFactory.apply(idSelectionOptions);

        return surveyInstanceDao.findBySurveyInstanceIdSelector(selector);
    }


    public List<SurveyInstance> findPreviousVersionsForInstance(long instanceId) {
        return surveyInstanceDao.findPreviousVersionsForInstance(instanceId);
    }


    public long addRecipient(SurveyInstanceRecipientCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        return surveyInstanceRecipientDao.create(command);
    }


    public boolean updateRecipient(SurveyInstanceRecipientUpdateCommand command) {
        checkNotNull(command, "command cannot be null");

        boolean delete = surveyInstanceRecipientDao.delete(command.instanceRecipientId());
        long id = surveyInstanceRecipientDao.create(ImmutableSurveyInstanceRecipientCreateCommand.builder()
                .personId(command.personId())
                .surveyInstanceId(command.surveyInstanceId())
                .build());
        return delete && id > 0;
    }


    public boolean delete(long surveyInstanceRecipientId) {
        return surveyInstanceRecipientDao.delete(surveyInstanceRecipientId);
    }

}
