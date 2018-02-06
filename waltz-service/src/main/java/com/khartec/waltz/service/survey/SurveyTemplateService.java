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
import com.khartec.waltz.data.survey.SurveyQuestionDao;
import com.khartec.waltz.data.survey.SurveyQuestionDropdownEntryDao;
import com.khartec.waltz.data.survey.SurveyTemplateDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.ListUtilities.map;

@Service
public class SurveyTemplateService {
    private final ChangeLogService changeLogService;

    private final PersonDao personDao;
    private final SurveyTemplateDao surveyTemplateDao;
    private final SurveyQuestionDao surveyQuestionDao;
    private final SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao;


    @Autowired
    public SurveyTemplateService(ChangeLogService changeLogService,
                                 PersonDao personDao,
                                 SurveyTemplateDao surveyTemplateDao,
                                 SurveyQuestionDao surveyQuestionDao,
                                 SurveyQuestionDropdownEntryDao surveyQuestionDropdownEntryDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(surveyTemplateDao, "surveyTemplateDao cannot be null");
        checkNotNull(surveyQuestionDao, "surveyQuestionDao cannot be null");
        checkNotNull(surveyQuestionDropdownEntryDao, "surveyQuestionDropdownEntryDao cannot be null");

        this.changeLogService = changeLogService;
        this.personDao = personDao;
        this.surveyTemplateDao = surveyTemplateDao;
        this.surveyQuestionDao = surveyQuestionDao;
        this.surveyQuestionDropdownEntryDao = surveyQuestionDropdownEntryDao;
    }


    public SurveyTemplate getById(long id) {
        return surveyTemplateDao.getById(id);
    }


    public List<SurveyTemplate> findAll(String userName) {
        checkNotNull(userName, "userName cannot be null");

        Person owner = personDao.getByUserName(userName);
        checkNotNull(owner, "userName " + userName + " cannot be resolved");

        return surveyTemplateDao.findAll(owner.id().get());
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
                .status(ReleaseLifecycleStatus.DRAFT)
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
                .stream()
                .forEach(sq -> {
                    SurveyQuestion clonedQuestion = ImmutableSurveyQuestion
                            .copyOf(sq)
                            .withId(Optional.empty())
                            .withSurveyTemplateId(newTemplateId);

                    long id = surveyQuestionDao.create(clonedQuestion);

                    if(sq.fieldType() == SurveyQuestionFieldType.DROPDOWN) {
                        //clone the entries too
                        List<SurveyQuestionDropdownEntry> existingEntries = surveyQuestionDropdownEntryDao.findForQuestion(sq.id().get());
                        List<SurveyQuestionDropdownEntry> clonedEntries = map(
                                existingEntries,
                                e -> ImmutableSurveyQuestionDropdownEntry.copyOf(e).withQuestionId(id));
                        surveyQuestionDropdownEntryDao.saveEntries(id, clonedEntries);
                    }
                });

        return newTemplateId;
    }
}
