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

package org.finos.waltz.service.change_initiative;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.ImmutableGenericSelector;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import org.finos.waltz.data.entity_relationship.EntityRelationshipDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.change_initiative.ChangeInitiativeView;
import org.finos.waltz.model.change_initiative.ImmutableChangeInitiativeView;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectOrderByStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.RATING_SCHEME_ITEM;

@Service
public class ChangeInitiativeViewService {

    private static final EntityHierarchy eh = EntityHierarchy.ENTITY_HIERARCHY;

    private final ChangeInitiativeDao changeInitiativeDao;
    private final EntityRelationshipDao relationshipDao;
    private final AssessmentRatingDao assessmentRatingDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final RatingSchemeDAO ratingSchemeDao;

    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory = new ChangeInitiativeIdSelectorFactory();

    @Autowired
    public ChangeInitiativeViewService(ChangeInitiativeDao changeInitiativeDao,
                                       EntityRelationshipDao relationshipDao,
                                       AssessmentRatingDao assessmentRatingDao, AssessmentDefinitionDao assessmentDefinitionDao, RatingSchemeDAO ratingSchemeDao)
    {
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(relationshipDao, "relationshipDao cannot be null");
        checkNotNull(assessmentRatingDao, "assessmentRatingDao cannot be null");
        checkNotNull(assessmentDefinitionDao, "assessmentDefinitionDao cannot be null");
        checkNotNull(ratingSchemeDao, "ratingSchemeDao cannot be null");

        this.changeInitiativeDao = changeInitiativeDao;
        this.relationshipDao = relationshipDao;
        this.assessmentRatingDao = assessmentRatingDao;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.ratingSchemeDao = ratingSchemeDao;
    }


    public ChangeInitiativeView getForEntityReference(EntityReference ref) {
        Select<Record1<Long>> directCISelector = changeInitiativeIdSelectorFactory.apply(mkOpts(ref));

        SelectOrderByStep<Record1<Long>> indirectCISelector = DSL
                .select(eh.ID)
                .from(eh)
                .where(eh.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())
                    .and(eh.ANCESTOR_ID.in(directCISelector)))
                .union(DSL
                    .select(eh.ANCESTOR_ID)
                    .from(eh)
                    .where(eh.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())
                            .and(eh.ID.in(directCISelector))));

        GenericSelector indirectGenericCISelector = ImmutableGenericSelector
                .builder()
                .selector(indirectCISelector)
                .kind(EntityKind.CHANGE_INITIATIVE).build();

        Collection<ChangeInitiative> cis = changeInitiativeDao.findForSelector(indirectCISelector);
        List<AssessmentRating> ratings = assessmentRatingDao.findByGenericSelector(indirectGenericCISelector);
        Set<AssessmentDefinition> defs = assessmentDefinitionDao.findByEntityKind(EntityKind.CHANGE_INITIATIVE);
        List<RatingSchemeItem> ratingSchemeItems = ratingSchemeDao.fetchItems(RATING_SCHEME_ITEM.SCHEME_ID.in(map(
                defs,
                AssessmentDefinition::ratingSchemeId)));

        return ImmutableChangeInitiativeView.builder()
                .changeInitiatives(cis)
                .ratings(ratings)
                .ratingSchemeItems(ratingSchemeItems)
                .assessmentDefinitions(defs)
                .build();
    }

}
