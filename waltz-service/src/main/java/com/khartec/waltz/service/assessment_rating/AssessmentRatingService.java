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

package com.khartec.waltz.service.assessment_rating;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.assessment_definition.AssessmentDefinitionDao;
import com.khartec.waltz.data.assessment_rating.AssessmentRatingDao;
import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_definition.AssessmentVisibility;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import com.khartec.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;

@Service
public class AssessmentRatingService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final ChangeLogService changeLogService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public AssessmentRatingService(
            AssessmentRatingDao assessmentRatingDao,
            AssessmentDefinitionDao assessmentDefinitionDao,
            RatingSchemeDAO ratingSchemeDAO,
            ChangeLogService changeLogService) {
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(ratingSchemeDAO, "ratingSchemeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.assessmentRatingDao = assessmentRatingDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.changeLogService = changeLogService;

    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        return assessmentRatingDao.findForEntity(ref);
    }


    public List<AssessmentRating> findByEntityKind(EntityKind targetKind, List<AssessmentVisibility> visibilities) {
        return assessmentRatingDao.findByEntityKind(targetKind, visibilities);
    }


    public List<AssessmentRating> findByTargetKindForRelatedSelector(EntityKind targetKind,
                                                                     IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);
        return assessmentRatingDao.findByGenericSelector(genericSelector);
    }


    public boolean store(SaveAssessmentRatingCommand command, String username) {
        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(command.assessmentDefinitionId());
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(format(
                        "Storing assessment %s as [%s - %s]",
                        assessmentDefinition.name(),
                        ratingSchemeDAO.getRagNameById(command.ratingId()).name(),
                        command.comment()))
                .parentReference(mkRef(command.entityReference().kind(), command.entityReference().id()))
                .userId(username)
                .severity(Severity.INFORMATION)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(logEntry);

        return assessmentRatingDao.store(command);
    }


    public boolean remove(RemoveAssessmentRatingCommand command, String username) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(format(
                        "Removed %s",
                        assessmentDefinitionDao.getById(command.assessmentDefinitionId()).name()))
                .parentReference(mkRef(
                        command.entityReference().kind(),
                        command.entityReference().id()))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.REMOVE)
                .build();

        changeLogService.write(logEntry);

        return assessmentRatingDao.remove(command);
    }
}
