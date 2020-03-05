/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyInstanceRecipientDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.data.survey.SurveyRunDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.user.UserRoleService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.OptionalUtilities.contentsEqual;
import static java.util.Optional.ofNullable;

@Service
public class SurveyInstanceService {

    private final ChangeLogService changeLogService;
    private final PersonDao personDao;
    private final SurveyInstanceDao surveyInstanceDao;
    private final SurveyInstanceRecipientDao surveyInstanceRecipientDao;
    private final SurveyQuestionResponseDao surveyQuestionResponseDao;
    private final SurveyInstanceIdSelectorFactory surveyInstanceIdSelectorFactory = new SurveyInstanceIdSelectorFactory();
    private SurveyRunDao surveyRunDao;
    private UserRoleService userRoleService;


    @Autowired
    public SurveyInstanceService(ChangeLogService changeLogService,
                                 PersonDao personDao,
                                 SurveyInstanceDao surveyInstanceDao,
                                 SurveyInstanceRecipientDao surveyInstanceRecipientDao,
                                 SurveyQuestionResponseDao surveyQuestionResponseDao,
                                 SurveyRunDao surveyRunDao,
                                 UserRoleService userRoleService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyInstanceDao, "surveyInstanceDao cannot be null");
        checkNotNull(surveyInstanceRecipientDao, "surveyInstanceRecipientDao cannot be null");
        checkNotNull(surveyQuestionResponseDao, "surveyQuestionResponseDao cannot be null");
        checkNotNull(surveyRunDao, "surveyRunDao cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.changeLogService = changeLogService;
        this.personDao = personDao;
        this.surveyInstanceDao = surveyInstanceDao;
        this.surveyInstanceRecipientDao = surveyInstanceRecipientDao;
        this.surveyQuestionResponseDao = surveyQuestionResponseDao;
        this.surveyRunDao = surveyRunDao;
        this.userRoleService = userRoleService;
    }


    public SurveyInstance getById(long instanceId) {
        return surveyInstanceDao.getById(instanceId);
    }


    public List<SurveyInstance> findForRecipient(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person person = getPersonByUsername(userName);

        return surveyInstanceDao.findForRecipient(person.id().get());
    }


    public List<SurveyInstance> findForRecipient(Long personId) {
        checkNotNull(personId, "personId cannot be null");

        return surveyInstanceDao.findForRecipient(personId);
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

        Person person = checkPersonIsRecipient(userName, instanceId);

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


    public Person checkPersonIsOwnerOrAdmin(String userName, long instanceId) {
        Person person = getPersonByUsername(userName);
        checkTrue(
                isAdmin(userName) || isOwner(instanceId, person) || hasOwningRole(instanceId, person),
                "Permission denied");
        return person;
    }


    public Person checkPersonIsRecipient(String userName, long instanceId) {
        Person person = getPersonByUsername(userName);
        boolean isPersonInstanceRecipient = surveyInstanceRecipientDao.isPersonInstanceRecipient(
                person.id().get(),
                instanceId);
        checkTrue(isPersonInstanceRecipient, "Permission denied");
        return person;
    }


    public int updateStatus(String userName, long instanceId, SurveyInstanceStatusChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        if (command.newStatus() != SurveyInstanceStatus.COMPLETED) {
            checkPersonIsOwnerOrAdmin(userName, instanceId);
        }

        SurveyInstance surveyInstance = surveyInstanceDao.getById(instanceId);

        // if survey is being sent back, store current responses as a version
        if ((surveyInstance.status() == SurveyInstanceStatus.COMPLETED || surveyInstance.status() == SurveyInstanceStatus.APPROVED)
                && command.newStatus() == SurveyInstanceStatus.REJECTED) {
            long versionedInstanceId = surveyInstanceDao.createPreviousVersion(surveyInstance);
            surveyQuestionResponseDao.cloneResponses(surveyInstance.id().get(), versionedInstanceId);
            surveyInstanceDao.clearApproved(instanceId);
        }

        if ((surveyInstance.status() == SurveyInstanceStatus.APPROVED || surveyInstance.status() == SurveyInstanceStatus.WITHDRAWN)
                && command.newStatus() == SurveyInstanceStatus.IN_PROGRESS) {
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

        checkPersonIsOwnerOrAdmin(userName, instanceId);
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

        checkPersonIsOwnerOrAdmin(userName, instanceId);
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


    public long addRecipient(String username, SurveyInstanceRecipientCreateCommand command) {
        checkNotNull(command, "command cannot be null");
        checkPersonIsOwnerOrAdmin(username, command.surveyInstanceId());
        long rc = surveyInstanceRecipientDao.create(command);

        logRecipientChange(
                username,
                command.surveyInstanceId(),
                command.personId(),
                Operation.ADD,
                "Survey Instance: Added %s as a recipient");

        return rc;
    }



    public boolean updateRecipient(String username, SurveyInstanceRecipientUpdateCommand command) {
        checkNotNull(command, "command cannot be null");
        checkPersonIsOwnerOrAdmin(username, command.surveyInstanceId());

        boolean delete = surveyInstanceRecipientDao.delete(command.instanceRecipientId());
        long id = surveyInstanceRecipientDao.create(ImmutableSurveyInstanceRecipientCreateCommand
                .builder()
                .personId(command.personId())
                .surveyInstanceId(command.surveyInstanceId())
                .build());

        logRecipientChange(
                username,
                command.surveyInstanceId(),
                command.personId(),
                Operation.UPDATE,
                "Survey Instance: Set %s as a recipient");

        return delete && id > 0;
    }


    public boolean deleteRecipient(String username, long surveyInstanceId, long recipientId) {
        checkPersonIsOwnerOrAdmin(username, surveyInstanceId);
        boolean rc = surveyInstanceRecipientDao.delete(recipientId);

        logRecipientChange(
                username,
                surveyInstanceId,
                recipientId,
                Operation.REMOVE,
                "Survey Instance: Removed %s as a recipient");

        return rc;
    }


    private Person getPersonByUsername(String userName) {
        Person person = personDao.getActiveByUserEmail(userName);
        checkNotNull(person, "userName %s cannot be resolved", userName);
        return person;
    }


    private Person getPersonById(Long id) {
        Person person = personDao.getById(id);
        checkNotNull(person, "Person with id %d cannot be resolved", id);
        return person;
    }


    private boolean isOwner(long instanceId, Person person) {
        SurveyInstance instance = surveyInstanceDao.getById(instanceId);
        SurveyRun run = surveyRunDao.getById(instance.surveyRunId());

        return contentsEqual(person.id(), run.ownerId());
    }


    private boolean isAdmin(String userName) {
        return userRoleService.hasRole(userName, SystemRole.SURVEY_ADMIN);
    }


    private boolean hasOwningRole(long instanceId, Person person) {
        SurveyInstance instance = surveyInstanceDao.getById(instanceId);
        return userRoleService.hasRole(person.email(), instance.owningRole());
    }


    private void logRecipientChange(String username, long instanceId, long personId, Operation op, String msg) {
        Person recipient = getPersonById(personId);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(op)
                        .userId(username)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_INSTANCE, instanceId))
                        .childKind(EntityKind.PERSON)
                        .message(String.format(msg, recipient.name()))
                        .build());
    }

}
