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

package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingChangeSummary;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.MeasurableRatingStatParams;
import org.finos.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import org.finos.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.tally.MeasurableRatingTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.*;

@Service
public class MeasurableRatingService {

    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableDao measurableDao;
    private final MeasurableCategoryDao measurableCategoryDao;
    private final ChangeLogService changeLogService;
    private final RatingSchemeService ratingSchemeService;
    private final EntityReferenceNameResolver entityReferenceNameResolver;

    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public MeasurableRatingService(MeasurableRatingDao measurableRatingDao,
                                   MeasurableDao measurableDao,
                                   MeasurableCategoryDao measurableCategoryDao,
                                   ChangeLogService changeLogService,
                                   RatingSchemeService ratingSchemeService,
                                   EntityReferenceNameResolver entityReferenceNameResolver) {
        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableCategoryDao, "measurableCategoryDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");

        this.measurableRatingDao = measurableRatingDao;
        this.measurableDao = measurableDao;
        this.measurableCategoryDao = measurableCategoryDao;
        this.changeLogService = changeLogService;
        this.ratingSchemeService = ratingSchemeService;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
    }

    // -- READ

    public List<MeasurableRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return measurableRatingDao.findForEntity(ref);
    }


    public List<MeasurableRating> findByMeasurableIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableRatingDao.findByMeasurableIdSelector(selector, options);
    }


    public Collection<MeasurableRating> findByAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return measurableRatingDao.findByApplicationIdSelector(selector);
    }

    public Collection<MeasurableRating> findByCategory(long id) {
        return measurableRatingDao.findByCategory(id);
    }


    // -- WRITE

    @Deprecated
    public Collection<MeasurableRating> save(SaveMeasurableRatingCommand command, boolean ignoreReadOnly) {
        checkNotNull(command, "command cannot be null");

        checkRatingIsAllowable(command);

        Measurable measurable = measurableDao.getById(command.measurableId());
        checkNotNull(measurable, format("Unknown measurable with id: %d", command.measurableId()));
        checkTrue(measurable.concrete(), "Cannot rate against an abstract measurable");

        Operation operationThatWasPerformed = measurableRatingDao.save(command, false);

        String entityName = getEntityName(command);

        String previousRatingMessage = command.previousRating().isPresent()
                ? "from " + command.previousRating().get() : "";

        writeChangeLogEntry(
                command,
                format("Saved: %s with a rating of: %s %s for %s",
                        measurable.name(),
                        command.rating(),
                        previousRatingMessage,
                        entityName),
                format("Saved: %s has assigned %s with a rating of: %s %s",
                        entityName,
                        measurable.name(),
                        command.rating(),
                        previousRatingMessage),
                operationThatWasPerformed);

        return findForEntity(command.entityReference());
    }


    /**
     * Removes all ratings for the given entity where the associated
     * measurable belongs to the given category.
     * @param ref EntityReference of the entity linked to the measurables
     * @param categoryId  Measurable Category identifier
     * @param username who is doing the removal
     * @return The remaining mappings for the given entity
     */
    public Collection<MeasurableRating> removeForCategory(EntityReference ref, long categoryId, String username) {
        checkNotNull(ref, "Cannot remove entity ratings for a category if the given entity reference is null");

        MeasurableCategory category = checkNotNull(
                measurableCategoryDao.getById(categoryId),
                "Cannot find category: %d", categoryId);

        int removedCount = measurableRatingDao.removeForCategory(ref, categoryId);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(format("Removed all (%d) ratings for category: %s which are not read-only", removedCount, category.name()))
                .parentReference(ref)
                .userId(username)
                .createdAt(DateTimeUtilities.nowUtc())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.MEASURABLE)
                .operation(Operation.REMOVE)
                .build());

        return findForEntity(ref);
    }


    public Collection<MeasurableRating> remove(RemoveMeasurableRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        Measurable measurable = measurableDao.getById(command.measurableId());

        boolean success = measurableRatingDao.remove(command);

        if (success && measurable != null) {
            String entityName = getEntityName(command);

            writeChangeLogEntry(
                    command,
                    format("Removed: %s for %s",
                            measurable.name(),
                            entityName),
                    format("Removed: %s for %s",
                            entityName,
                            measurable.name()),
                    Operation.REMOVE);

        }
        return findForEntity(command.entityReference());
    }


    public int deleteByMeasurableIdSelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory
                .apply(selectionOptions);
        return measurableRatingDao
                .deleteByMeasurableIdSelector(selector);
    }


    public boolean saveRatingItem(EntityReference entityRef,
                                  long measurableId,
                                  String ratingCode,
                                  String username) {
        long categoryId = measurableDao.getById(measurableId).categoryId();
        checkRatingIsAllowable(categoryId, entityRef, ratingCode);

        MeasurableRatingChangeSummary loggingInfo = measurableRatingDao.resolveLoggingContextForRatingChange(
                entityRef,
                measurableId,
                ratingCode);

        boolean rc = measurableRatingDao.saveRatingItem(
                entityRef,
                measurableId,
                ratingCode,
                username);

        if (rc) {
            writeChangeLogEntry(
                    entityRef,
                    loggingInfo.measurableRef(),
                    format("Saving rating for: %s, in category: %s, new value: %s, old value: %s",
                           prettyRef(loggingInfo.measurableRef()),
                           prettyRef(loggingInfo.measurableCategoryRef()),
                           prettyRating(loggingInfo.desiredRatingNameAndCode()),
                           prettyRating(loggingInfo.currentRatingNameAndCode())),
                    format("Saving rating for application %s, new value: %s, old value: %s",
                           prettyRef(loggingInfo.entityRef()),
                           prettyRating(loggingInfo.desiredRatingNameAndCode()),
                           prettyRating(loggingInfo.currentRatingNameAndCode())),
                    loggingInfo.currentRatingNameAndCode() == null
                            ? Operation.ADD
                            : Operation.UPDATE,
                    UserTimestamp.mkForUser(username));
        }

        return rc;
    }


    public boolean saveRatingIsPrimary(EntityReference entityRef,
                                       long measurableId,
                                       boolean isPrimary,
                                       String username) {

        MeasurableRatingChangeSummary loggingInfo = measurableRatingDao.resolveLoggingContextForRatingChange(
                entityRef,
                measurableId,
                null);

        boolean rc = measurableRatingDao.saveRatingIsPrimary(
                entityRef,
                measurableId,
                isPrimary,
                username);

        if (rc) {
            writeChangeLogEntry(
                    entityRef,
                    loggingInfo.measurableRef(),
                    format("Setting primary rating flag for: %s, in category: %s",
                           prettyRef(loggingInfo.measurableRef()),
                           prettyRef(loggingInfo.measurableCategoryRef())),
                    format("Setting primary flag for application: %s",
                           prettyRef(loggingInfo.entityRef())),
                    Operation.UPDATE,
                    UserTimestamp.mkForUser(username));
        }

        return rc;
    }


    public boolean saveRatingDescription(EntityReference entityRef,
                                         long measurableId,
                                         String description,
                                         String username) {

        MeasurableRatingChangeSummary loggingInfo = measurableRatingDao.resolveLoggingContextForRatingChange(
                entityRef,
                measurableId,
                null);

        boolean rc = measurableRatingDao.saveRatingDescription(entityRef, measurableId, description, username);

        if (rc) {
            writeChangeLogEntry(
                    entityRef,
                    loggingInfo.measurableRef(),
                    format("Updating description for rating: %s, in category: %s, new value: %s",
                           prettyRef(loggingInfo.measurableRef()),
                           prettyRef(loggingInfo.measurableCategoryRef()),
                           description),
                    format("Updated rating description for application: %s, new value: %s",
                           prettyRef(loggingInfo.entityRef()),
                           description),
                    Operation.UPDATE,
                    UserTimestamp.mkForUser(username));
        }

        return rc;
    }


    // STATS

    public List<Tally<Long>> tallyByMeasurableCategoryId(long categoryId) {
        return measurableRatingDao.tallyByMeasurableCategoryId(categoryId);
    }


    public List<MeasurableRatingTally> statsByAppSelector(MeasurableRatingStatParams params) {
        checkNotNull(params, "params cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(params.options());
        return measurableRatingDao.statsByAppSelector(
                selector,
                params.showPrimaryOnly());
    }


    public boolean hasImplicitlyRelatedMeasurables(long measurableId, IdSelectionOptions options) {
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return measurableRatingDao.hasImplicitlyRelatedMeasurables(measurableId, selector);
    }


    public String getRequiredRatingEditRole(EntityReference ref) {
        return measurableDao.getRequiredRatingEditRole(ref);
    }


    private String getEntityName(MeasurableRatingCommand command) {
        EntityReference entityReference = command.entityReference().name().isPresent()
                ? command.entityReference()
                : entityReferenceNameResolver.resolve(command.entityReference()).orElse(command.entityReference());
        return entityReference.name().orElse("");
    }


    @Deprecated
    public boolean checkRatingExists(SaveMeasurableRatingCommand command) {
        return measurableRatingDao.checkRatingExists(command);
    }


    public void migrateRatings(Long measurableId, Long targetMeasurableId, String userId) {
        measurableRatingDao.migrateRatings(measurableId, targetMeasurableId, userId);
    }


    public int getSharedRatingsCount(Long measurableId, Long targetMeasurableId) {
        return measurableRatingDao.getSharedRatingsCount(measurableId, targetMeasurableId);
    }


    public int getSharedDecommsCount(Long measurableId, Long targetMeasurableId) {
        return measurableRatingDao.getSharedDecommsCount(measurableId, targetMeasurableId);
    }


    // ---- HELPER -----

    private void writeChangeLogEntry(EntityReference ratedEntity,
                                     EntityReference measurable,
                                     String message1,
                                     String message2,
                                     Operation operation,
                                     UserTimestamp userTimestamp) {

        changeLogService.write(ImmutableChangeLog.builder()
                                                 .message(message1)
                                                 .parentReference(ratedEntity)
                                                 .userId(userTimestamp.by())
                                                 .createdAt(userTimestamp.at())
                                                 .severity(Severity.INFORMATION)
                                                 .childKind(EntityKind.MEASURABLE)
                                                 .operation(operation)
                                                 .build());

        changeLogService.write(ImmutableChangeLog.builder()
                                                 .message(message2)
                                                 .parentReference(measurable)
                                                 .userId(userTimestamp.by())
                                                 .createdAt(userTimestamp.at())
                                                 .severity(Severity.INFORMATION)
                                                 .childKind(ratedEntity.kind())
                                                 .operation(operation)
                                                 .build());
    }


    @Deprecated
    private void writeChangeLogEntry(MeasurableRatingCommand command,
                                     String message1,
                                     String message2,
                                     Operation operation) {

        writeChangeLogEntry(
                command.entityReference(),
                EntityReference.mkRef(EntityKind.MEASURABLE, command.measurableId()),
                message1,
                message2,
                operation,
                command.lastUpdate());
    }



    @Deprecated
    private void checkRatingIsAllowable(SaveMeasurableRatingCommand command) {

        long measurableCategory = measurableDao.getById(command.measurableId()).categoryId();
        EntityReference entityReference = command.entityReference();
        String ratingCode = Character.toString(command.rating());

        checkRatingIsAllowable(measurableCategory, entityReference, ratingCode);
    }


    /**
     * Checks
     *
     * @param measurableCategory  category to check against, indirectly gives the rating scheme
     * @param entityReference  the entity the rating is against
     * @param ratingCode  the rating code that is being checked
     */
    private void checkRatingIsAllowable(long measurableCategory,
                                        EntityReference entityReference,
                                        String ratingCode) {
        Boolean isRestricted = ratingSchemeService
                .findRatingSchemeItemsForEntityAndCategory(entityReference, measurableCategory)
                .stream()
                .filter(r -> r.rating().equals(ratingCode))
                .map(RatingSchemeItem::isRestricted)
                .findFirst()
                .orElse(false);

        checkFalse(isRestricted, "New rating is restricted, rating not saved");
    }


    private String prettyRef(EntityReference ref) {
        return ref == null
                ? "-"
                : format("%s [%d]", ref.name().orElse("?"), ref.id());
    }


    private String prettyRating(Tuple2<String, String> nameAndCode) {
        return nameAndCode == null
                ? "-"
                : format("%s [%s]", nameAndCode.v1, nameAndCode.v2);
    }



}
