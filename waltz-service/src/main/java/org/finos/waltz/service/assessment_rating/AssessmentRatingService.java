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

package org.finos.waltz.service.assessment_rating;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.permission.permission_checker.AssessmentRatingPermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class AssessmentRatingService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final ChangeLogService changeLogService;
    private final AssessmentRatingPermissionChecker assessmentRatingPermissionChecker;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public AssessmentRatingService(
            AssessmentRatingDao assessmentRatingDao,
            AssessmentDefinitionDao assessmentDefinitionDao,
            RatingSchemeDAO ratingSchemeDAO,
            ChangeLogService changeLogService,
            AssessmentRatingPermissionChecker assessmentRatingPermissionChecker) {

        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(ratingSchemeDAO, "ratingSchemeDao cannot be null");
        checkNotNull(assessmentRatingPermissionChecker, "ratingPermissionChecker cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.assessmentRatingPermissionChecker = assessmentRatingPermissionChecker;
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


    public boolean store(SaveAssessmentRatingCommand command, String username) throws InsufficientPrivelegeException {
        verifyAnyPermission(asSet(Operation.UPDATE, Operation.ADD), command.entityReference(), command.assessmentDefinitionId(), command.ratingId(), username);
        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(command.assessmentDefinitionId());
        createChangeLogEntryForSave(command, username, assessmentDefinition);

        return assessmentRatingDao.store(command);
    }


    public boolean lock(EntityReference entityReference,
                        long assessmentDefinitionId,
                        long ratingId,
                        String username) throws InsufficientPrivelegeException {

        verifyPermission(Operation.LOCK, entityReference, assessmentDefinitionId, ratingId, username);
        return assessmentRatingDao.lock(entityReference, assessmentDefinitionId, ratingId, username);
    }


    public boolean unlock(EntityReference entityReference,
                          long assessmentDefinitionId,
                          long ratingId,
                          String username) throws InsufficientPrivelegeException {
        verifyPermission(
                Operation.LOCK,
                entityReference,
                assessmentDefinitionId,
                ratingId,
                username);
        return assessmentRatingDao.unlock(
                entityReference,
                assessmentDefinitionId,
                ratingId,
                username);
    }


    public boolean remove(RemoveAssessmentRatingCommand command, String username) throws InsufficientPrivelegeException {

        verifyPermission(
                Operation.REMOVE,
                command.entityReference(),
                command.assessmentDefinitionId(),
                command.ratingId(),
                username);

        String ratingRemovedName = ratingSchemeDAO.findRatingSchemeItemsByIds(asSet(command.ratingId()))
                .stream()
                .map(d -> d.name())
                .collect(Collectors.joining(", "));

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(format(
                        "Removed assessment: %s, rating: %s",
                        assessmentDefinitionDao.getById(command.assessmentDefinitionId()).name(),
                        ratingRemovedName))
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


    private Set<AssessmentRating> getRatingsFilterByOperation(BulkAssessmentRatingCommand[] commands,
                                                              long assessmentDefinitionId,
                                                              String username,
                                                              Operation operation) {
        return Arrays.stream(commands)
                .filter(c -> c.operation().equals(operation))
                .map(command -> getAssessmentRating(command, assessmentDefinitionId, username))
                .collect(Collectors.toSet());
    }


    public AssessmentDefinitionRatingOperations getRatingPermissions(EntityReference entityReference, long assessmentDefinitionId, String username) {
        return assessmentRatingPermissionChecker
                .getRatingPermissions(entityReference, assessmentDefinitionId, username);
    }


    public boolean update(long id, String comment, String username) throws InsufficientPrivelegeException {
        AssessmentRating existingRating = assessmentRatingDao.getById(id);
        verifyAnyPermission(asSet(Operation.UPDATE, Operation.ADD),
                existingRating.entityReference(),
                existingRating.assessmentDefinitionId(),
                existingRating.ratingId(),
                username);
        createUpdateCommentChangeLogs(id, comment, username); // do first so that comment from old rating retained
        return assessmentRatingDao.updateComment(id, comment, username);
    }


    // region HELPERS

    private void verifyPermission(Operation requiredPerm,
                                  EntityReference ref,
                                  long defId,
                                  long ratingId,
                                  String username) throws InsufficientPrivelegeException {
        verifyAnyPermission(
                asSet(requiredPerm),
                ref,
                defId,
                ratingId,
                username);
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


    private void verifyAnyPermission(Set<Operation> possiblePerms,
                                     EntityReference ref,
                                     long defId,
                                     long ratingId,
                                     String username) throws InsufficientPrivelegeException {

        AssessmentDefinitionRatingOperations definitionRatingPerms = assessmentRatingPermissionChecker.getRatingPermissions(ref, defId, username);

        Set<Operation> permsForRating = definitionRatingPerms.findForRatingId(ratingId);

        assessmentRatingPermissionChecker.verifyAnyPerms(possiblePerms, permsForRating, EntityKind.ASSESSMENT_DEFINITION, username);
    }


    private void createChangeLogEntryForSave(SaveAssessmentRatingCommand command,
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
                                        StringUtilities.ifEmpty(r.comment(), ""),
                                        r.entityReference().name().orElse("")))
                                .parentReference(r.entityReference())
                                .userId(username)
                                .severity(Severity.INFORMATION)
                                .operation(operation)
                                .build())
                .collect(Collectors.toSet());

        changeLogService.write(logs);
    }

    private void createUpdateCommentChangeLogs(long id,
                                               String comment,
                                               String username) {

        AssessmentRating rating = assessmentRatingDao.getById(id);
        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(rating.assessmentDefinitionId());
        RatingSchemeItem ratingItem = ratingSchemeDAO.getRatingSchemeItemById(rating.ratingId());

        ImmutableChangeLog log = ImmutableChangeLog.builder()
                .message(format(
                        "Updated comment for assessment '%s', rating '%s' from: '%s' to '%s'",
                        assessmentDefinition.name(),
                        ratingItem.name(),
                        rating.comment(),
                        comment))
                .parentReference(rating.entityReference())
                .userId(username)
                .severity(Severity.INFORMATION)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(asSet(log));
    }

    public Set<AssessmentRatingSummaryCounts> findRatingSummaryCounts(EntityKind targetKind,
                                                                      IdSelectionOptions idSelectionOptions,
                                                                      Set<Long> definitionIds) {

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, idSelectionOptions);
        return assessmentRatingDao.findRatingSummaryCounts(genericSelector, definitionIds);
    }
}
