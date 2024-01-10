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

package org.finos.waltz.service.application;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.measurable.MeasurableDao;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.ApplicationsView;
import org.finos.waltz.model.application.ImmutableApplicationsView;
import org.finos.waltz.model.application.ImmutableAssessmentsView;
import org.finos.waltz.model.application.ImmutableMeasurableRatingsView;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.utils.IdUtilities.toIds;
import static org.finos.waltz.schema.Tables.MEASURABLE;


@Service
public class ApplicationViewService {

    private static final GenericSelectorFactory GENERIC_SELECTOR_FACTORY = new GenericSelectorFactory();

    private final ApplicationDao applicationDao;
    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final MeasurableCategoryDao measurableCategoryDao;
    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableDao measurableDao;

    @Autowired
    public ApplicationViewService(ApplicationDao appDao,
                                  AssessmentRatingDao assessmentRatingDao,
                                  AssessmentDefinitionDao assessmentDefinitionDao,
                                  RatingSchemeDAO ratingSchemeDAO,
                                  MeasurableCategoryDao measurableCategoryDao,
                                  MeasurableRatingDao measurableRatingDao,
                                  MeasurableDao measurableDao) {
        this.applicationDao = appDao;
        this.assessmentRatingDao = assessmentRatingDao;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.measurableCategoryDao = measurableCategoryDao;
        this.measurableRatingDao = measurableRatingDao;
        this.measurableDao = measurableDao;
    }


    public ApplicationsView getViewBySelector(IdSelectionOptions selectionOptions) {

        GenericSelector genericSelector = GENERIC_SELECTOR_FACTORY.applyForKind(EntityKind.APPLICATION, selectionOptions);
        List<Application> apps = applicationDao.findByAppIdSelector(genericSelector.selector());

        Set<AssessmentDefinition> primaryAssessmentDefs = assessmentDefinitionDao
                .findPrimaryDefinitionsForKind(
                    EntityKind.APPLICATION,
                    Optional.empty());

        Set<AssessmentRating> assessmentRatings = assessmentRatingDao.findBySelectorForDefinitions(
                genericSelector,
                toIds(primaryAssessmentDefs));

        Set<RatingSchemeItem> assessmentRatingSchemeItems = ratingSchemeDAO.findRatingSchemeItemsByIds(
                map(assessmentRatings, AssessmentRating::ratingId));

        Set<MeasurableCategory> categories = measurableCategoryDao
                .findAll()
                .stream()
                .filter(MeasurableCategory::allowPrimaryRatings)
                .collect(Collectors.toSet());

        Set<MeasurableRating> primaryMeasurableRatings = measurableRatingDao
                .findPrimaryRatingsForGenericSelector(genericSelector);

        List<Measurable> measurables = measurableDao.findByMeasurableIdSelector(DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.ID.in(map(
                        primaryMeasurableRatings,
                        MeasurableRating::measurableId))));

        Set<RatingSchemeItem> measurableRatingSchemeItems = ratingSchemeDAO
                .findRatingSchemeItemsForSchemeIds(
                    map(categories, MeasurableCategory::ratingSchemeId));

        return ImmutableApplicationsView
                .builder()
                .applications(apps)
                .primaryAssessments(ImmutableAssessmentsView
                        .builder()
                        .assessmentDefinitions(primaryAssessmentDefs)
                        .assessmentRatings(assessmentRatings)
                        .ratingSchemeItems(assessmentRatingSchemeItems)
                        .build())
                .primaryRatings(ImmutableMeasurableRatingsView
                        .builder()
                        .measurableCategories(categories)
                        .measurables(measurables)
                        .ratingSchemeItems(measurableRatingSchemeItems)
                        .measurableRatings(primaryMeasurableRatings)
                        .build())
                .build();
    }
}

