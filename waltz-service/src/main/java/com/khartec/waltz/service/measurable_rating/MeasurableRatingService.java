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

package com.khartec.waltz.service.measurable_rating;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.measurable_rating.MeasurableRatingCommand;
import com.khartec.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import com.khartec.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.tally.MeasurableRatingTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.*;
import static java.lang.String.format;

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

    // -- WRITE

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

        measurableRatingDao.removeForCategory(ref, categoryId);

        changeLogService.write(ImmutableChangeLog.builder()
                .message(format("Removed all ratings for category: %s", category.name()))
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


    public List<Tally<Long>> tallyByMeasurableCategoryId(long categoryId) {
        return measurableRatingDao.tallyByMeasurableCategoryId(categoryId);
    }

    public Collection<MeasurableRatingTally> statsForRelatedMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableRatingDao.statsForRelatedMeasurable(selector);
    }


    public List<MeasurableRatingTally> statsByAppSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return measurableRatingDao.statsByAppSelector(selector);
    }


    // -- HELPERS --

    private void writeChangeLogEntry(MeasurableRatingCommand command,
                                     String message1,
                                     String message2,
                                     Operation operation) {

        changeLogService.write(ImmutableChangeLog.builder()
                .message(message1)
                .parentReference(command.entityReference())
                .userId(command.lastUpdate().by())
                .createdAt(command.lastUpdate().at())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.MEASURABLE)
                .operation(operation)
                .build());

        changeLogService.write(ImmutableChangeLog.builder()
                .message(message2)
                .parentReference(EntityReference.mkRef(EntityKind.MEASURABLE, command.measurableId()))
                .userId(command.lastUpdate().by())
                .createdAt(command.lastUpdate().at())
                .severity(Severity.INFORMATION)
                .childKind(command.entityReference().kind())
                .operation(operation)
                .build());
    }


    public Collection<MeasurableRating> findByCategory(long id) {
        return measurableRatingDao.findByCategory(id);
    }


    public int deleteByMeasurableIdSelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory
                .apply(selectionOptions);
        return measurableRatingDao
                .deleteByMeasurableIdSelector(selector);
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


    private void checkRatingIsAllowable(SaveMeasurableRatingCommand command) {

        long measurableCategory = measurableDao.getById(command.measurableId()).categoryId();
        EntityReference entityReference = command.entityReference();

        Boolean isRestricted = ratingSchemeService
                .findRatingSchemeItemsForEntityAndCategory(entityReference, measurableCategory)
                .stream()
                .filter(r -> r.rating().equals(command.rating()))
                .map(RagName::isRestricted)
                .findFirst()
                .orElse(false);

        checkFalse(isRestricted, "New rating is restricted, rating not saved");
    }
}
