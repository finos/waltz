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

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.user.UserPreferenceService;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_rating.*;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.user.UserPreference;
import org.jooq.Condition;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.splitThenMap;
import static org.jooq.tools.StringUtils.toCamelCaseLC;

@Service
public class AssessmentRatingViewService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final UserPreferenceService userPreferenceService;

    private final String ASSESSMENT_PREFERENCE_KEY = "main.app-view.assessment-rating.favouriteAssessmentDefnIds%s";

    @Autowired
    public AssessmentRatingViewService(
            AssessmentRatingDao assessmentRatingDao,
            AssessmentDefinitionDao assessmentDefinitionDao,
            RatingSchemeDAO ratingSchemeDAO,
            ChangeLogService changeLogService, UserPreferenceService userPreferenceService) {
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(ratingSchemeDAO, "ratingSchemeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.userPreferenceService = userPreferenceService;
        this.assessmentRatingDao = assessmentRatingDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
    }


    public Collection<AssessmentGroupedEntities> findGroupedByDefinitionAndOutcomes(EntityKind kind, List<Long> entityIds) {

        Condition entityCondition = isEmpty(entityIds)
                ? ASSESSMENT_RATING.ENTITY_KIND.eq(kind.name())
                : ASSESSMENT_RATING.ENTITY_KIND.eq(kind.name()).and(ASSESSMENT_RATING.ENTITY_ID.in(entityIds));

        Map<Long, AssessmentDefinition> definitionsById = indexBy(assessmentDefinitionDao.findAll(), def -> def.id().get());

        Set<Tuple2<Long, Set<ImmutableRatingEntityList>>> groupedByDefinitionAndOutcome = assessmentRatingDao
                .findGroupedByDefinitionAndOutcome(entityCondition);

        return map(
                groupedByDefinitionAndOutcome,
                rel -> ImmutableAssessmentGroupedEntities
                        .builder()
                        .assessmentDefinition(definitionsById.get(rel.v1))
                        .ratingEntityLists(rel.v2)
                        .build());
    }


    public Set<AssessmentRatingDetail> findFavouriteAssessmentsForEntityAndUser(EntityReference ref, String username){

        Set<AssessmentDefinition> allDefns = assessmentDefinitionDao.findAll();
        Map<Long, AssessmentDefinition> definitionsById = indexBy(allDefns, d -> d.id().get());

        List<Long> assessmentDefinitionIds = determineFavoriteAssessments(username, allDefns, ref.kind());

        List<AssessmentRating> assessmentRatings = assessmentRatingDao.findForEntity(ref);

        Set<Long> ratingIds = map(assessmentRatings, AssessmentRating::ratingId);

        Map<Long, RatingSchemeItem> ratingItemsById = indexBy(
                ratingSchemeDAO.findRatingSchemeItemsByIds(ratingIds),
                d -> d.id().get());

        return assessmentRatings
                .stream()
                .filter(d -> assessmentDefinitionIds.contains(d.assessmentDefinitionId()))
                .map(d -> ImmutableAssessmentRatingDetail.builder()
                        .assessmentRating(d)
                        .assessmentDefinition(definitionsById.get(d.assessmentDefinitionId()))
                        .ratingDefinition(ratingItemsById.get(d.ratingId()))
                        .build())
                .collect(Collectors.toSet());
    }


    private List<Long> determineFavoriteAssessments(String username, Set<AssessmentDefinition> allDefns, EntityKind kind) {

        List<Long> defaultAssessmentIds = allDefns
                .stream()
                .filter(d -> d.visibility().equals(AssessmentVisibility.PRIMARY))
                .map(d -> d.id().get())
                .collect(Collectors.toList());

        List<UserPreference> preferences = userPreferenceService.getPreferences(username);

        List<Long> assessmentDefinitionIds = maybeFirst(
                preferences,
                d -> {
                    String favouriteAssessmentsKey = format(ASSESSMENT_PREFERENCE_KEY, toCamelCaseLC(kind.name()));
                    return d.key().equalsIgnoreCase(favouriteAssessmentsKey);
                })
                .map(d -> splitThenMap(d.value(), ",", Long::valueOf))
                .orElse(defaultAssessmentIds);

        return assessmentDefinitionIds;
    }

}