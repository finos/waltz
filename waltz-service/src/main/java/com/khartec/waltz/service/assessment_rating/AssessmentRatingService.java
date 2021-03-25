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

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.assessment_definition.AssessmentDefinitionDao;
import com.khartec.waltz.data.assessment_rating.AssessmentRatingDao;
import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_rating.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.rating.RatingSchemeItem;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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


    public List<AssessmentRating> findByEntityKind(EntityKind targetKind) {
        return assessmentRatingDao.findByEntityKind(targetKind);
    }


    public List<AssessmentRating> findByTargetKindForRelatedSelector(EntityKind targetKind,
                                                                     IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);
        return assessmentRatingDao.findByGenericSelector(genericSelector);
    }


    public List<AssessmentRating> findByDefinitionId(long definitionId) {

        return assessmentRatingDao.findByDefinitionId(definitionId);
    }

    public boolean store(SaveAssessmentRatingCommand command, String username) {
        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(command.assessmentDefinitionId());
        createChangeLogEntry(command, username, assessmentDefinition);

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

    public boolean bulkStore(BulkAssessmentRatingCommand[] commands,
                             long assessmentDefinitionId,
                             String username) {
        Set<AssessmentRating> ratingsToAdd = getRatingsFilterByOperation(commands, assessmentDefinitionId, username, Operation.ADD);
        int addedResult = assessmentRatingDao.add(ratingsToAdd);
        createChangeLogs(assessmentDefinitionId, username, ratingsToAdd, Operation.ADD);

        Set<AssessmentRating> ratingsToUpdate = getRatingsFilterByOperation(commands, assessmentDefinitionId, username, Operation.UPDATE);
        int updateResult = assessmentRatingDao.update(ratingsToUpdate);
        createChangeLogs(assessmentDefinitionId, username, ratingsToUpdate, Operation.ADD);

        return addedResult + updateResult > 1;
    }

    public boolean bulkDelete(BulkAssessmentRatingCommand[] commands,
                              long assessmentDefinitionId,
                              String username) {
        Set<AssessmentRating> ratingsToRemove = getRatingsFilterByOperation(commands, assessmentDefinitionId, username, Operation.REMOVE);
        createChangeLogs(assessmentDefinitionId, username, ratingsToRemove, Operation.REMOVE);
        int result = assessmentRatingDao.remove(ratingsToRemove);

        return result  > 1;
    }

    private void createChangeLogEntry(SaveAssessmentRatingCommand command,
                                      String username,
                                      AssessmentDefinition assessmentDefinition) {
        Optional<AssessmentRating>  previousRating = assessmentRatingDao.findForEntity(command.entityReference())
                .stream()
                .filter(r -> r.assessmentDefinitionId() == command.assessmentDefinitionId())
                .findAny();
        Optional<RatingSchemeItem> previousRatingSchemeItem = previousRating.map(assessmentRating -> ratingSchemeDAO.getRatingSchemeItemById(assessmentRating.ratingId()));
        Optional<String> messagePostfix = previousRatingSchemeItem
                .map(rn -> format(" from assessment %s as [%s - %s]",
                        assessmentDefinition.name(),
                        rn.name(),
                        previousRating.get().comment()));

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(format(
                        "Storing assessment %s as [%s - %s]%s",
                        assessmentDefinition.name(),
                        ratingSchemeDAO.getRatingSchemeItemById(command.ratingId()).name(),
                        command.comment(),
                        messagePostfix.orElse("")))
                .parentReference(mkRef(command.entityReference().kind(), command.entityReference().id()))
                .userId(username)
                .severity(Severity.INFORMATION)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(logEntry);
    }

    private void createChangeLogs(long assessmentDefinitionId,
                                  String username,
                                  Set<AssessmentRating> ratingsToAdd,
                                  Operation operation) {

        String messagePrefix = operation.equals(Operation.REMOVE) ? "Removed" : "Added";

        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(assessmentDefinitionId);
        Map<Long, RatingSchemeItem> ratingItems = MapUtilities.indexBy(
                ratingSchemeDAO.findRatingSchemeItemsForAssessmentDefinition(assessmentDefinitionId),
                r -> r.id().orElse(0L));

        Set<ChangeLog> logs = ratingsToAdd.stream()
                .map(r ->
                        ImmutableChangeLog.builder()
                                .message(messagePrefix + format(
                                        " assessment %s as [%s - %s] for %s",
                                        assessmentDefinition.name(),
                                        ratingItems.get(r.ratingId()).name(),
                                        StringUtilities.ifEmpty(r.comment(),""),
                                        r.entityReference().name().orElse("")))
                                .parentReference(r.entityReference())
                                .userId(username)
                                .severity(Severity.INFORMATION)
                                .operation(operation)
                                .build())
                .collect(Collectors.toSet());

        changeLogService.write(logs);
    }

    private Set<AssessmentRating> getRatingsFilterByOperation(BulkAssessmentRatingCommand[] commands,
                                                              long assessmentDefinitionId,
                                                              String username, Operation operation) {
        return Arrays.stream(commands)
                .filter(c -> c.operation().equals(operation))
                .map(command -> getAssessmentRating(command, assessmentDefinitionId, username))
                .collect(Collectors.toSet());
    }

    private AssessmentRating getAssessmentRating(BulkAssessmentRatingCommand command,
                                                 Long assessmentDefinitionId,
                                                 String username) {
        UserTimestamp lastUpdate = UserTimestamp.mkForUser(username);
        return ImmutableAssessmentRating.builder()
                .assessmentDefinitionId(assessmentDefinitionId)
                .entityReference(command.entityRef())
                .ratingId(command.ratingId())
                .comment(command.comment())
                .lastUpdatedAt(lastUpdate.at())
                .lastUpdatedBy(lastUpdate.by())
                .provenance("waltz")
                .build();
    }
}
