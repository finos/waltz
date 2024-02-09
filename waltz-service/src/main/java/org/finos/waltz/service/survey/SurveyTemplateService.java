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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import org.finos.waltz.data.survey.SurveyTemplateDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.ReleaseLifecycleStatusChangeCommand;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.ImmutableSurveyQuestion;
import org.finos.waltz.model.survey.ImmutableSurveyQuestionDropdownEntry;
import org.finos.waltz.model.survey.ImmutableSurveyTemplate;
import org.finos.waltz.model.survey.ImmutableSurveyTemplateChangeCommand;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.finos.waltz.model.survey.SurveyRunWithOwnerAndStats;
import org.finos.waltz.model.survey.SurveyTemplate;
import org.finos.waltz.model.survey.SurveyTemplateChangeCommand;
import org.finos.waltz.model.survey_template_exchange.SurveyQuestionModel;
import org.finos.waltz.model.survey_template_exchange.SurveyTemplateExchange;
import org.finos.waltz.model.survey_template_exchange.SurveyTemplateModel;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.map;

@Service
public class SurveyTemplateService {
    private static final Logger LOG = LoggerFactory.getLogger(SurveyTemplateService.class);

    private final ChangeLogService changeLogService;
    private final PersonDao personDao;
    private final SurveyTemplateDao surveyTemplateDao;
    private final SurveyRunService surveyRunService;
    private final SurveyQuestionDao surveyQuestionDao;
    private final SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao;


    @Autowired
    public SurveyTemplateService(ChangeLogService changeLogService,
                                 PersonDao personDao,
                                 SurveyTemplateDao surveyTemplateDao,
                                 SurveyRunService surveyRunService,
                                 SurveyQuestionDao surveyQuestionDao,
                                 SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");
        checkNotNull(surveyRunService, "surveyRunService cannot be null");
        checkNotNull(surveyQuestionDao, "surveyQuestionDao cannot be null");
        checkNotNull(surveyQuestionDropdownEntryDao, "surveyQuestionDropdownEntryDao cannot be null");

        this.changeLogService = changeLogService;
        this.personDao = personDao;
        this.surveyTemplateDao = surveyTemplateDao;
        this.surveyRunService = surveyRunService;
        this.surveyQuestionDao = surveyQuestionDao;
        this.surveyQuestionDropdownEntryDao = surveyQuestionDropdownEntryDao;
    }


    public SurveyTemplate getById(long id) {
        return surveyTemplateDao.getById(id);
    }


    public List<SurveyTemplate> findForOwner(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person owner = personDao.getActiveByUserEmail(userName);

        if (owner == null) {
            return surveyTemplateDao.findForOwner(null);
        } else {
            return surveyTemplateDao.findForOwner(owner.id().orElse(null));
        }
    }


    public long create(String userName, SurveyTemplateChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        Person owner = personDao.getActiveByUserEmail(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved to a person record");

        long surveyTemplateId = surveyTemplateDao.create(ImmutableSurveyTemplate.builder()
                .name(command.name())
                .description(command.description())
                .targetEntityKind(command.targetEntityKind())
                .ownerId(owner.id().get())
                .createdAt(DateTimeUtilities.nowUtc())
                .status(ReleaseLifecycleStatus.DRAFT)
                .externalId(command.externalId())
                .issuanceRole(command.issuanceRole())
                .build());

        changeLogService.write(
            ImmutableChangeLog.builder()
                .operation(Operation.ADD)
                .userId(userName)
                .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, surveyTemplateId))
                .message(format("Survey Template: '%s' added", command.name()))
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


    public int updateStatus(String userName, long templateId, ReleaseLifecycleStatusChangeCommand command) {
        checkNotNull(command, "command cannot be null");

        int result = surveyTemplateDao.updateStatus(templateId, command.newStatus());

        if (result > 0) {
            changeLogService.write(
                    ImmutableChangeLog.builder()
                            .operation(Operation.UPDATE)
                            .userId(userName)
                            .parentReference(EntityReference.mkRef(EntityKind.SURVEY_TEMPLATE, templateId))
                            .message("Survey Template: status changed to " + command.newStatus())
                            .build());
        }

        return result;
    }


    public long clone(String userName, long sourceTemplateId) {
        checkNotNull(userName, "userName cannot be null");

        SurveyTemplate sourceTemplate = surveyTemplateDao.getById(sourceTemplateId);
        checkNotNull(sourceTemplate, "sourceTemplate cannot be null");

        // clone template properties
        SurveyTemplateChangeCommand templateChangeCommand = ImmutableSurveyTemplateChangeCommand.builder()
                .name(sourceTemplate.name() + " - (clone)")
                .description(sourceTemplate.description())
                .targetEntityKind(sourceTemplate.targetEntityKind())
                .build();

        long newTemplateId = create(userName, templateChangeCommand);

        // clone questions
        List<SurveyQuestion> sourceQuestions = surveyQuestionDao.findForTemplate(sourceTemplateId);

        sourceQuestions
                .forEach(sq -> {
                    SurveyQuestion clonedQuestion = ImmutableSurveyQuestion
                            .copyOf(sq)
                            .withId(Optional.empty())
                            .withSurveyTemplateId(newTemplateId);

                    long id = surveyQuestionDao.create(clonedQuestion);

                    if(sq.fieldType() == SurveyQuestionFieldType.DROPDOWN
                            || sq.fieldType() == SurveyQuestionFieldType.DROPDOWN_MULTI_SELECT) {
                        //clone the entries too
                        // TODO: use the more efficient findQuestionsForTemplate method
                        List<SurveyQuestionDropdownEntry> existingEntries = surveyQuestionDropdownEntryDao.findForQuestion(sq.id().get());
                        List<SurveyQuestionDropdownEntry> clonedEntries = map(
                                existingEntries,
                                e -> ImmutableSurveyQuestionDropdownEntry.copyOf(e).withQuestionId(id));
                        surveyQuestionDropdownEntryDao.saveEntries(id, clonedEntries);
                    }
                });

        return newTemplateId;
    }


    public Boolean delete(long id) {
        List<SurveyRunWithOwnerAndStats> runs = surveyRunService.findByTemplateId(id);

        if (isEmpty(runs)) {
            surveyQuestionDropdownEntryDao.deleteForTemplate(id);
            surveyQuestionDao.deleteForTemplate(id);
            return surveyTemplateDao.delete(id);
        } else {
            throw new IllegalArgumentException("Cannot delete a template that has runs");
        }
    }


    public SurveyTemplate getByQuestionId(long questionId) {
        return surveyTemplateDao.getByQuestionId(questionId);
    }


    public Collection<SurveyTemplate> findAll() {
        return surveyTemplateDao.findAll();
    }


    public Long importTemplateFromJSON(String username,
                                       SurveyTemplateExchange templateExchange) {
        SurveyTemplate model = toModelFromJSON(templateExchange.template(), username);
        long templateId = surveyTemplateDao.create(model);

        templateExchange
                .questions()
                .forEach(q -> {
                    SurveyQuestion modelQ = toQuestionFromJSON(templateId, q);
                    long qId = surveyQuestionDao.create(modelQ);
                    Set<SurveyQuestionDropdownEntry> entries = toDropDownEntriesFromJSON(q, qId);

                    if (! entries.isEmpty()) {
                        surveyQuestionDropdownEntryDao.saveEntries(
                                qId,
                                entries);
                    }
                });

        LOG.info(
                "User: {}, created template {} from json model",
                username,
                templateId);

        return templateId;
    }

    private Set<SurveyQuestionDropdownEntry> toDropDownEntriesFromJSON(SurveyQuestionModel q,
                                                                              long qId) {
        Set<SurveyQuestionDropdownEntry> entries = q
                .dropdownEntries()
                .stream()
                .map(d -> ImmutableSurveyQuestionDropdownEntry
                        .builder()
                        .questionId(qId)
                        .position(d.position())
                        .value(d.value())
                        .build())
                .collect(toSet());
        return entries;
    }

    private SurveyQuestion toQuestionFromJSON(long templateId,
                                             SurveyQuestionModel q) {
        SurveyQuestion modelQ = ImmutableSurveyQuestion
                .builder()
                .surveyTemplateId(templateId)
                .questionText(q.questionText())
                .label(ofNullable(q.label()))
                .externalId(q.externalId())
                .allowComment(q.allowComment())
                .fieldType(q.fieldType())
                .qualifierEntity(ofNullable(q.qualifierEntity()))
                .helpText(ofNullable(q.helpText()))
                .inclusionPredicate(ofNullable(q.inclusionPredicate()))
                .position(q.position())
                .isMandatory(q.isMandatory())
                .sectionName(ofNullable(q.sectionName()))
                .parentExternalId(ofNullable(q.parentExternalId()))
                .build();
        return modelQ;
    }


    private SurveyTemplate toModelFromJSON(SurveyTemplateModel from,
                                           String username) {

        Collection<SurveyTemplate> currentTemplates = surveyTemplateDao.findAll();

        Long ownerId = determineOwnerId(from, username);
        String extId = determineExternalIdToUse(from, currentTemplates);
        String name = determineNameToUse(from, currentTemplates);

        return ImmutableSurveyTemplate
                .builder()
                .name(name)
                .externalId(extId)
                .description(from.description())
                .targetEntityKind(from.targetEntityKind())
                .status(ReleaseLifecycleStatus.DRAFT)
                .ownerId(ownerId)
                .build();
    }


    private Long determineOwnerId(SurveyTemplateModel template,
                                  String username) {
        return personDao
                .getByEmployeeId(template.ownerEmployeeId())
                .id()
                .orElseGet(() -> personDao
                        .getByUserEmail(username)
                        .id()
                        .orElseThrow(() -> new IllegalArgumentException(format(
                                "Cannot determine owner based on employee id: %s, or fallback user: %s ",
                                template.ownerEmployeeId(),
                                username))));
    }


    private String determineExternalIdToUse(SurveyTemplateModel template,
                                            Collection<SurveyTemplate> currentTemplates) {
        Set<String> usedExtIds = SetUtilities.map(
                currentTemplates,
                d -> d.externalId().orElse(null));

        return template
                .externalId()
                .filter(d -> !usedExtIds.contains(d))
                .orElse(UUID.randomUUID().toString());
    }


    private String determineNameToUse(SurveyTemplateModel template,
                                      Collection<SurveyTemplate> currentTemplates) {
        Set<String> usedNames = SetUtilities.map(
                currentTemplates,
                NameProvider::name);

        String candidateName = template.name();

        return usedNames.contains(candidateName)
                ? format("%s - [%s]", candidateName, UUID.randomUUID())
                : candidateName;
    }
}
