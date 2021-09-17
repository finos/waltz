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

import com.khartec.waltz.data.assessment_definition.AssessmentDefinitionDao;
import com.khartec.waltz.data.assessment_rating.AssessmentRatingDao;
import com.khartec.waltz.data.rating_scheme.RatingSchemeDAO;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_rating.AssessmentGroupedEntities;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentGroupedEntities;
import com.khartec.waltz.model.assessment_rating.ImmutableRatingEntityList;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Condition;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.schema.Tables.ASSESSMENT_RATING;

@Service
public class AssessmentRatingViewService {

    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDAO;


    @Autowired
    public AssessmentRatingViewService(
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

}