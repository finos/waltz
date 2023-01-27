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

package org.finos.waltz.service.survey;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.survey.*;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.*;

@Service
public class SurveyRunService {

    private final ChangeLogService changeLogService;
    private final InvolvementDao involvementDao;
    private final PersonDao personDao;
    private final SurveyInstanceDao surveyInstanceDao;
    private final SurveyInstanceRecipientDao surveyInstanceRecipientDao;
    private final SurveyInstanceOwnerDao surveyInstanceOwnerDao;
    private final SurveyRunDao surveyRunDao;
    private final SurveyTemplateDao surveyTemplateDao;
    private final SurveyQuestionResponseDao surveyQuestionResponseDao;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final SurveyInstanceIdSelectorFactory surveyInstanceIdSelectorFactory = new SurveyInstanceIdSelectorFactory();


    @Autowired
    public SurveyRunService(ChangeLogService changeLogService,
                            InvolvementDao involvementDao,
                            PersonDao personDao,
                            SurveyInstanceDao surveyInstanceDao,
                            SurveyInstanceRecipientDao surveyInstanceRecipientDao,
                            SurveyInstanceOwnerDao surveyInstanceOwnerDao,
                            SurveyRunDao surveyRunDao,
                            SurveyTemplateDao surveyTemplateDao,
                            SurveyQuestionResponseDao surveyQuestionResponseDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(involvementDao, "involvementDao cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyInstanceDao, "surveyInstanceDao cannot be null");
        checkNotNull(surveyInstanceRecipientDao, "surveyInstanceRecipientDao cannot be null");
        checkNotNull(surveyInstanceOwnerDao, "surveyInstanceOwnerDao cannot be null");
        checkNotNull(surveyRunDao, "surveyRunDao cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");
        checkNotNull(surveyQuestionResponseDao, "surveyQuestionResponseDao cannot be null");

        this.changeLogService = changeLogService;
        this.involvementDao = involvementDao;
        this.personDao = personDao;
        this.surveyInstanceDao = surveyInstanceDao;
        this.surveyInstanceRecipientDao = surveyInstanceRecipientDao;
        this.surveyInstanceOwnerDao = surveyInstanceOwnerDao;
        this.surveyRunDao = surveyRunDao;
        this.surveyTemplateDao = surveyTemplateDao;
        this.surveyQuestionResponseDao = surveyQuestionResponseDao;
    }


    public SurveyRun getById(long id) {
        return surveyRunDao.getById(id);
    }


    public List<SurveyRun> findForRecipient(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person person = personDao.getActiveByUserEmail(userName);
        checkNotNull(person, "userName " + userName + " cannot be resolved");

        return surveyRunDao.findForRecipient(person.id().get());
    }


    public List<SurveyRun> findForRecipient(Long personId) {
        checkNotNull(personId, "personId cannot be null");

        return surveyRunDao.findForRecipient(personId);
    }


    public IdCommandResponse createSurveyRun(String userName, SurveyRunCreateCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "create command cannot be null");

        Person owner = personDao.getActiveByUserEmail(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        long surveyRunId = surveyRunDao.create(owner.id().get(), command);

        // log against template
        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, command.surveyTemplateId()))
                        .childKind(EntityKind.SURVEY_RUN)
                        .message("Survey Run: " + command.name() + " (ID: " + surveyRunId + ") added")
                        .build());


        // log against run
        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: " + command.name() + " added")
                        .build());

        return ImmutableIdCommandResponse.builder()
                .id(surveyRunId)
                .build();
    }


    public boolean deleteSurveyRun(String userName, long surveyRunId) {
        checkNotNull(userName, "userName cannot be null");

        validateSurveyRunDelete(userName, surveyRunId);

        SurveyRun surveyRun = surveyRunDao.getById(surveyRunId);

        // delete question responses
        surveyQuestionResponseDao.deleteForSurveyRun(surveyRunId);
        // delete instance recipients
        surveyInstanceRecipientDao.deleteForSurveyRun(surveyRunId);
        // delete instances
        surveyInstanceDao.deleteForSurveyRun(surveyRunId);
        // delete run
        boolean deleteSuccessful = surveyRunDao.delete(surveyRunId) == 1;

        // log against template
        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.REMOVE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, surveyRun.surveyTemplateId()))
                        .childKind(EntityKind.SURVEY_RUN)
                        .message("Survey Run: " + surveyRun.name() + " (ID: " + surveyRunId + ") removed")
                        .build());

        // log against run (for completeness)
        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.REMOVE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: " + surveyRun.name() + " removed")
                        .build());

        return deleteSuccessful;
    }


    public int updateSurveyRun(String userName, long surveyRunId, SurveyRunChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "change command cannot be null");

        validateSurveyRunUpdate(userName, surveyRunId);

        return surveyRunDao.update(surveyRunId, command);
    }


    public int updateSurveyRunStatus(String userName, long surveyRunId, SurveyRunStatus newStatus) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(newStatus, "newStatus cannot be null");

        validateSurveyRunUpdate(userName, surveyRunId);

        int result = (newStatus == SurveyRunStatus.ISSUED)
                ? surveyRunDao.issue(surveyRunId)
                : surveyRunDao.updateStatus(surveyRunId, newStatus);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: status changed to " + newStatus)
                        .build());

        return result;
    }


    public int updateSurveyRunDueDate(String userName, long surveyRunId, DateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        LocalDate newDueDate = command.newDateVal().orElse(null);

        checkNotNull(newDueDate, "newDueDate cannot be null");

        int surveyRunResult = surveyRunDao.updateDueDate(surveyRunId, newDueDate);
        int surveyInstanceResult = surveyInstanceDao.updateDueDateForSurveyRun(surveyRunId, newDueDate);

        checkApprovalDueDateIsLaterThanSubmissionDueDate(surveyRunDao.getById(surveyRunId).approvalDueDate(), newDueDate);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: due date changed to " + newDueDate)
                        .build());

        return surveyRunResult + surveyInstanceResult;
    }


    public int updateSurveyRunApprovalDueDate(String userName, long surveyRunId, DateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        LocalDate newDueDate = command.newDateVal().orElse(null);
        checkNotNull(newDueDate, "newDueDate cannot be null");
        checkApprovalDueDateIsLaterThanSubmissionDueDate(newDueDate, surveyRunDao.getById(surveyRunId).dueDate());

        int surveyRunResult = surveyRunDao.updateApprovalDueDate(surveyRunId, newDueDate);
        int surveyInstanceResult = surveyInstanceDao.updateApprovalDueDateForSurveyRun(surveyRunId, newDueDate);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(EntityReference.mkRef(EntityKind.SURVEY_RUN, surveyRunId))
                        .message("Survey Run: approval due date changed to " + newDueDate)
                        .build());

        return surveyRunResult + surveyInstanceResult;
    }


    public List<SurveyInstanceRecipient> generateSurveyInstanceRecipients(InstancesAndRecipientsCreateCommand command) {
        SurveyRun surveyRun = surveyRunDao.getById(command.surveyRunId());
        checkNotNull(surveyRun, "surveyRun " + command.surveyRunId() + " not found");

        SurveyTemplate surveyTemplate = surveyTemplateDao.getById(surveyRun.surveyTemplateId());
        checkNotNull(surveyTemplate, "surveyTemplate " + surveyRun.surveyTemplateId() + " not found");

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(surveyTemplate.targetEntityKind(), surveyRun.selectionOptions());
        Map<EntityReference, List<Person>> entityRefToPeople = involvementDao.findPeopleByEntitySelectorAndInvolvement(
                surveyTemplate.targetEntityKind(),
                genericSelector.selector(),
                surveyRun.involvementKindIds());

        return entityRefToPeople.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream()
                        .map(p -> ImmutableSurveyInstanceRecipient.builder()
                                .surveyInstance(ImmutableSurveyInstance.builder()
                                        .surveyEntity(e.getKey())
                                        .surveyRunId(surveyRun.id().get())
                                        .status(SurveyInstanceStatus.NOT_STARTED)
                                        .dueDate(command.dueDate())
                                        .approvalDueDate(command.approvalDueDate())
                                        .owningRole(command.owningRole())
                                        .name(surveyRun.name())
                                        .build())
                                .person(p)
                                .build()))
                .distinct()
                .collect(toList());
    }


    public List<SurveyInstanceOwner> generateSurveyInstanceOwners(InstancesAndRecipientsCreateCommand command) {
        SurveyRun surveyRun = surveyRunDao.getById(command.surveyRunId());
        checkNotNull(surveyRun, "surveyRun " + command.surveyRunId() + " not found");

        SurveyTemplate surveyTemplate = surveyTemplateDao.getById(surveyRun.surveyTemplateId());
        checkNotNull(surveyTemplate, "surveyTemplate " + surveyRun.surveyTemplateId() + " not found");

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(surveyTemplate.targetEntityKind(), surveyRun.selectionOptions());
        Map<EntityReference, List<Person>> entityRefToPeople = involvementDao.findPeopleByEntitySelectorAndInvolvement(
                surveyTemplate.targetEntityKind(),
                genericSelector.selector(),
                surveyRun.ownerInvKindIds());

        return entityRefToPeople.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream()
                        .map(p -> ImmutableSurveyInstanceOwner.builder()
                                .surveyInstance(ImmutableSurveyInstance.builder()
                                        .surveyEntity(e.getKey())
                                        .surveyRunId(command.surveyRunId())
                                        .status(SurveyInstanceStatus.NOT_STARTED)
                                        .dueDate(command.dueDate())
                                        .approvalDueDate(command.approvalDueDate())
                                        .owningRole(command.owningRole())
                                        .build())
                                .person(p)
                                .build()))
                .distinct()
                .collect(toList());


    }


    public boolean createSurveyInstancesAndRecipients(InstancesAndRecipientsCreateCommand command) {

        SurveyRun surveyRun = surveyRunDao.getById(command.surveyRunId());
        checkNotNull(surveyRun, "surveyRun " + command.surveyRunId() + " not found");

        Set<SurveyInstanceRecipient> excludedRecipientSet = fromCollection(command.excludedRecipients());
        List<SurveyInstanceRecipient> surveyInstanceRecipients = generateSurveyInstanceRecipients(command)
                .stream()
                .filter(r -> !excludedRecipientSet.contains(r))
                .collect(toList());

        List<SurveyInstanceOwner> surveyInstanceOwners = generateSurveyInstanceOwners(command);

        Map<SurveyInstance, Collection<SurveyInstanceOwner>> surveyOwnersByInstance = groupBy(surveyInstanceOwners, SurveyInstanceOwner::surveyInstance);

        Map<SurveyInstance, List<SurveyInstanceRecipient>> instancesAndRecipientsToSave = surveyInstanceRecipients
                .stream()
                .collect(groupingBy(
                        SurveyInstanceRecipient::surveyInstance,
                        toList()
                ));

        // delete existing instances and recipients
        deleteSurveyInstancesAndRecipients(command.surveyRunId());

        // insert new instances and recipients
        instancesAndRecipientsToSave.forEach(
                (k,v) -> {
                    if (surveyRun.issuanceKind() == SurveyIssuanceKind.GROUP) {
                        // one instance per group
                        long instanceId = createSurveyInstance(k);
                        createSurveyInstanceOwner(instanceId, surveyRun.ownerId());
                        Collection<SurveyInstanceOwner> owners = surveyOwnersByInstance.get(k);
                        fromCollection(owners).forEach(o -> createSurveyInstanceOwner(instanceId, o.person().id().get()));
                        v.forEach(r -> createSurveyInstanceRecipient(instanceId, r));
                    } else {
                        // one instance for each individual
                        v.forEach(r -> {
                            long instanceId = createSurveyInstance(k);
                            createSurveyInstanceOwner(instanceId, surveyRun.ownerId());
                            Collection<SurveyInstanceOwner> owners = surveyOwnersByInstance.get(k);
                            fromCollection(owners).forEach(o -> createSurveyInstanceOwner(instanceId, o.person().id().get()));
                            createSurveyInstanceRecipient(instanceId, r);
                        });
                    }
                }
        );

        return true;
    }


    private void deleteSurveyInstancesAndRecipients(long surveyRunId) {
        surveyInstanceRecipientDao.deleteForSurveyRun(surveyRunId);
        surveyInstanceDao.deleteForSurveyRun(surveyRunId);
    }


    private long createSurveyInstance(SurveyInstance surveyInstance) {
        return surveyInstanceDao
                .create(ImmutableSurveyInstanceCreateCommand.builder()
                        .surveyRunId(surveyInstance.surveyRunId())
                        .entityReference(surveyInstance.surveyEntity())
                        .status(surveyInstance.status())
                        .dueDate(surveyInstance.dueDate())
                        .approvalDueDate(surveyInstance.approvalDueDate())
                        .owningRole(surveyInstance.owningRole())
                        .name(surveyInstance.name())
                        .build());
    }


    private long createSurveyInstanceRecipient(long surveyInstanceId, SurveyInstanceRecipient surveyInstanceRecipient) {
        return surveyInstanceRecipientDao.create(ImmutableSurveyInstanceRecipientCreateCommand.builder()
                .surveyInstanceId(surveyInstanceId)
                .personId(surveyInstanceRecipient.person().id().get())
                .build());
    }

    private long createSurveyInstanceOwner(long surveyInstanceId, Long ownerId) {
        return surveyInstanceOwnerDao.create(ImmutableSurveyInstanceOwnerCreateCommand.builder()
                .surveyInstanceId(surveyInstanceId)
                .personId(ownerId)
                .build());
    }


    private void validateSurveyRunUpdate(String userName, long surveyRunId) {
        Person owner = validateUser(userName);
        SurveyRun surveyRun = validateSurveyRun(surveyRunId);

        checkTrue(Objects.equals(surveyRun.ownerId(), owner.id().get()), "Permission denied");

        checkTrue(surveyRun.status() == SurveyRunStatus.DRAFT, "survey run can only be updated when it's still in DRAFT mode");
    }


    private void validateSurveyRunDelete(String userName, long surveyRunId) {
        Person owner = validateUser(userName);
        SurveyRun surveyRun = validateSurveyRun(surveyRunId);

        checkTrue(Objects.equals(surveyRun.ownerId(), owner.id().get()), "Permission denied");
    }


    private Person validateUser(String userName) {
        Person owner = personDao.getActiveByUserEmail(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        return owner;
    }


    private SurveyRun validateSurveyRun(long surveyRunId) {
        SurveyRun surveyRun = surveyRunDao.getById(surveyRunId);
        checkNotNull(surveyRun, "surveyRun " + surveyRunId + " not found");

        return surveyRun;
    }


    public List<SurveyRun> findBySurveyInstanceIdSelector(IdSelectionOptions idSelectionOptions) {
        checkNotNull(idSelectionOptions,  "idSelectionOptions cannot be null");

        Select<Record1<Long>> selector = surveyInstanceIdSelectorFactory.apply(idSelectionOptions);

        return surveyRunDao.findBySurveyInstanceIdSelector(selector);
    }


    public List<SurveyRunWithOwnerAndStats> findByTemplateId(long templateId) {
        List<SurveyRun> runs = surveyRunDao.findByTemplateId(templateId);
        Set<Person> owners = personDao.findByIds(SetUtilities.map(runs, SurveyRun::ownerId));
        Map<Long, Person> ownersById = indexBy(owners, p -> p.id().orElse(-1L));

        List<SurveyRunCompletionRate> stats = surveyInstanceDao.findCompletionRateForSurveyTemplate(templateId);
        Map<Long, SurveyRunCompletionRate> statsByRunId = indexBy(stats, s -> s.surveyRunId());

        return map(runs, run -> {
            Person owner = ownersById.get(run.ownerId());
            Long runId = run.id().orElse(-1L);
            SurveyRunCompletionRate completionRateStats = statsByRunId.getOrDefault(
                    runId,
                    SurveyRunCompletionRate.mkNoData(runId));

            return ImmutableSurveyRunWithOwnerAndStats.builder()
                    .surveyRun(run)
                    .owner(owner)
                    .completionRateStats(completionRateStats)
                    .build();
        });
    }


    public SurveyRunCompletionRate getCompletionRate(long surveyRunId) {
        return surveyInstanceDao.getCompletionRateForSurveyRun(surveyRunId);
    }


    public boolean createDirectSurveyInstances(long runId, SurveyInstanceRecipientsAndOwners recipientsAndOwners) {
        SurveyRun run = getById(runId);
        EntityReference subjectRef = run.selectionOptions().entityReference();
        Set<Long> surveyOwnerList = asSet(run.ownerId());

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(subjectRef.kind(), IdSelectionOptions.mkOpts(subjectRef, HierarchyQueryScope.EXACT));

        Set<Long> recipientPersonIdsFromKindIds = getPersonIdsFromInvKindIds(run.involvementKindIds(), subjectRef, genericSelector);
        Set<Long> ownerPersonIdsFromKindIds = getPersonIdsFromInvKindIds(run.ownerInvKindIds(), subjectRef, genericSelector);

        Set<Long> recipientIds = union(recipientPersonIdsFromKindIds, recipientsAndOwners.recipientPersonIds());
        Set<Long> ownerIds = union(ownerPersonIdsFromKindIds, recipientsAndOwners.ownerPersonIds(), surveyOwnerList);

        Set<Long> recipientsToBeIssuedSurveys = isEmpty(recipientIds)
                ? surveyOwnerList
                : recipientIds;

        switch (run.issuanceKind()) {
            case INDIVIDUAL:
                //create one survey per recipient
                recipientsToBeIssuedSurveys
                        .forEach(pId -> mkSurveyInstance(
                                subjectRef,
                                run,
                                asSet(pId),
                                ownerIds,
                                recipientsAndOwners.owningRole()));
                return true;
            case GROUP:
                mkSurveyInstance(
                        subjectRef,
                        run,
                        recipientsToBeIssuedSurveys,
                        ownerIds,
                        recipientsAndOwners.owningRole());
                return true;
            default:
                return false;
        }
    }

    private Set<Long> getPersonIdsFromInvKindIds(Set<Long> involvementKindIds, EntityReference subjectRef, GenericSelector genericSelector) {

        if (isEmpty(involvementKindIds)) {
            return emptySet();
        } else {

            Map<EntityReference, List<Person>> refToRecipientsByKind = involvementDao
                    .findPeopleByEntitySelectorAndInvolvement(
                            genericSelector.kind(),
                            genericSelector.selector(),
                            involvementKindIds);

            return refToRecipientsByKind.get(subjectRef)
                    .stream()
                    .map(p -> p.id().get())
                    .collect(Collectors.toSet());
        }
    }


    private void mkSurveyInstance(EntityReference entityRef,
                                  SurveyRun run,
                                  Set<Long> recipientIds,
                                  Set<Long> ownersIds,
                                  String owningRole) {

        SurveyInstanceCreateCommand instanceCreateCommand = ImmutableSurveyInstanceCreateCommand
                .builder()
                .dueDate(run.dueDate())
                .approvalDueDate(run.approvalDueDate())
                .entityReference(entityRef)
                .surveyRunId(run.id().get())
                .status(SurveyInstanceStatus.NOT_STARTED)
                .owningRole(owningRole)
                .name(run.name())
                .build();

        long instanceId = surveyInstanceDao.create(instanceCreateCommand);

        int[] recipientsCreated = surveyInstanceDao.createInstanceRecipients(
                instanceId,
                recipientIds);

        int[] ownersCreated = surveyInstanceDao.createInstanceOwners(
                instanceId,
                ownersIds);
    }


    public int updateSurveyInstanceOwningRoles(String username,
                                               long id,
                                               SurveyInstanceOwningRoleSaveCommand owningRoleSaveCommand) {

        return surveyInstanceDao.updateOwningRoleForSurveyRun(id, owningRoleSaveCommand.owningRole());
    }


    private void checkApprovalDueDateIsLaterThanSubmissionDueDate(LocalDate approvalDue, LocalDate submissionDue) {
        checkTrue(
                approvalDue.compareTo(submissionDue) >= 0,
                "Approval due date cannot be earlier than the submission due date");
    }

}

