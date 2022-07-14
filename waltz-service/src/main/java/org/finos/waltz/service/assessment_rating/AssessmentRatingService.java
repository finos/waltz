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
import org.finos.waltz.common.SetUtilities;
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
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.hasIntersection;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class AssessmentRatingService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final ChangeLogService changeLogService;
    private final PermissionGroupService permissionGroupService;
    private final InvolvementService involvementService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public AssessmentRatingService(
            AssessmentRatingDao assessmentRatingDao,
            AssessmentDefinitionDao assessmentDefinitionDao,
            RatingSchemeDAO ratingSchemeDAO,
            ChangeLogService changeLogService,
            PermissionGroupService permissionGroupService,
            InvolvementService involvementService) {
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(ratingSchemeDAO, "ratingSchemeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(involvementService, "involvementService cannot be null");
        checkNotNull(permissionGroupService, "permissionGroupService cannot be null");

        this.permissionGroupService = permissionGroupService;
        this.assessmentRatingDao = assessmentRatingDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.involvementService = involvementService;
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
        verifyAnyPermission(SetUtilities.asSet(Operation.UPDATE, Operation.ADD), command.entityReference(), command.assessmentDefinitionId(), username);
        AssessmentDefinition assessmentDefinition = assessmentDefinitionDao.getById(command.assessmentDefinitionId());
        createChangeLogEntryForSave(command, username, assessmentDefinition);

        return assessmentRatingDao.store(command);
    }


    public boolean lock(EntityReference entityReference,
                        long assessmentDefinitionId,
                        String username) throws InsufficientPrivelegeException {

        verifyPermission(Operation.LOCK, entityReference, assessmentDefinitionId, username);
        return assessmentRatingDao.lock(entityReference, assessmentDefinitionId, username);
    }


    public boolean unlock(EntityReference entityReference,
                          long assessmentDefinitionId,
                          String username) throws InsufficientPrivelegeException {
        verifyPermission(
                Operation.LOCK,
                entityReference,
                assessmentDefinitionId,
                username);
        return assessmentRatingDao.unlock(
                entityReference,
                assessmentDefinitionId,
                username);
    }


    public boolean remove(RemoveAssessmentRatingCommand command, String username) throws InsufficientPrivelegeException {

        verifyPermission(
                Operation.REMOVE,
                command.entityReference(),
                command.assessmentDefinitionId(),
                username);

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


    public Set<Operation> findRatingPermissions(EntityReference entityReference,
                                                long assessmentDefinitionId,
                                                String username) {

        Set<Long> invsForUser = involvementService.findExistingInvolvementKindIdsForUser(entityReference, username);

        Set<Operation> operationsForEntityAssessment = permissionGroupService
                .findPermissionsForParentReference(entityReference, username)
                .stream()
                .filter(p -> p.subjectKind().equals(EntityKind.ASSESSMENT_RATING)
                        && p.parentKind().equals(entityReference.kind())
                        && p.qualifierReference()
                        .map(ref -> mkRef(EntityKind.ASSESSMENT_DEFINITION, assessmentDefinitionId).equals(ref))
                        .orElse(false))
                .filter(p -> p.requiredInvolvementsResult().isAllowed(invsForUser))
                .map(Permission::operation)
                .collect(Collectors.toSet());

        return assessmentRatingDao.calculateAmendedRatingOperations(
                operationsForEntityAssessment,
                entityReference,
                assessmentDefinitionId,
                username);
    }


    // region HELPERS

    private void verifyPermission(Operation requiredPerm,
                                  EntityReference ref,
                                  long defId,
                                  String username) throws InsufficientPrivelegeException {
        verifyAnyPermission(
                asSet(requiredPerm),
                ref,
                defId,
                username);
    }


    private void verifyAnyPermission(Set<Operation> possiblePerms,
                                     EntityReference ref,
                                     long defId,
                                     String username) throws InsufficientPrivelegeException {

        Set<Operation> permsUserHas = findRatingPermissions(ref, defId, username);
        if (! hasIntersection(permsUserHas, possiblePerms)) {
            throw new InsufficientPrivelegeException(format(
                    "%s does not have any of the permissions: %s, for assessment def: %d.  They have: %s",
                    username,
                    possiblePerms,
                    defId,
                    permsUserHas));
        }
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

    // endregion


}
