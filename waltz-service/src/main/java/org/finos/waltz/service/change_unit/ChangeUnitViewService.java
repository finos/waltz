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

package org.finos.waltz.service.change_unit;

import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.assessment_rating.AssessmentRatingDetail;
import org.finos.waltz.model.assessment_rating.ImmutableAssessmentRatingDetail;
import org.finos.waltz.model.change_unit.ChangeUnit;
import org.finos.waltz.model.change_unit.ImmutablePhysicalFlowChangeUnitViewItem;
import org.finos.waltz.model.change_unit.PhysicalFlowChangeUnitViewItem;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOptsForAllLifecycleStates;


@Service
public class ChangeUnitViewService {

    private final PhysicalFlowService physicalFlowService;
    private final LogicalFlowService logicalFlowService;
    private final RatingSchemeService ratingSchemeService;
    private final AssessmentRatingService assessmentRatingService;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final ChangeUnitService changeUnitService;


    @Autowired
    public ChangeUnitViewService(PhysicalFlowService physicalFlowService,
                                 LogicalFlowService logicalFlowService,
                                 RatingSchemeService ratingSchemeService,
                                 AssessmentRatingService assessmentRatingService,
                                 PhysicalSpecificationService physicalSpecificationService,
                                 ChangeUnitService changeUnitService) {

        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        checkNotNull(changeUnitService, "changeUnitService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.logicalFlowService = logicalFlowService;
        this.ratingSchemeService = ratingSchemeService;
        this.assessmentRatingService = assessmentRatingService;
        this.physicalSpecificationService = physicalSpecificationService;
        this.changeUnitService = changeUnitService;
    }


    public Set<PhysicalFlowChangeUnitViewItem> findPhysicalFlowChangeUnitsByChangeSetId(long changeSetId){

        IdSelectionOptions idSelectionOptions = mkOptsForAllLifecycleStates(mkRef(EntityKind.CHANGE_SET, changeSetId), HierarchyQueryScope.EXACT);

        Collection<PhysicalFlow> physicalFlows = physicalFlowService.findBySelector(idSelectionOptions);
        Collection<PhysicalSpecification> physicalSpecs = physicalSpecificationService.findByIds(map(physicalFlows, PhysicalFlow::specificationId));
        Collection<LogicalFlow> logicalFlows = logicalFlowService.findAllByFlowIds(map(physicalFlows, PhysicalFlow::logicalFlowId)); //TODO: Move to a logical flow selector based upon change set #5626
        List<AssessmentRating> assessmentRatings = assessmentRatingService.findByTargetKindForRelatedSelector(EntityKind.CHANGE_UNIT, idSelectionOptions);

        Map<Long, RatingSchemeItem> ratingSchemeItemsById = indexBy(ratingSchemeService.findAllRatingSchemeItems(), item -> item.id().get());
        Map<Long, PhysicalFlow> physicalFlowsById = indexBy(physicalFlows, flow -> flow.id().get());
        Map<Long, LogicalFlow> logicalFlowsById = indexBy(logicalFlows, flow -> flow.id().get());
        Map<Long, PhysicalSpecification> specsById = indexBy(physicalSpecs, spec -> spec.id().get());
        Map<Long, Collection<AssessmentRating>> assessmentRatingsByEntityId = groupBy(rating -> rating.entityReference().id(), assessmentRatings);

        List<ChangeUnit> changeUnits = changeUnitService.findByChangeSetId(changeSetId);

        return changeUnits
                .stream()
                .filter(cu -> cu.subjectEntity().kind().equals(EntityKind.PHYSICAL_FLOW))
                .map(cu -> {

                    PhysicalFlow physicalFlow = physicalFlowsById.get(cu.subjectEntity().id());
                    PhysicalSpecification spec = specsById.get(physicalFlow.specificationId());
                    LogicalFlow logicalFlow = logicalFlowsById.get(physicalFlow.logicalFlowId());

                    Long changeUnitId = cu.id().get();
                    Collection<AssessmentRating> assessmentRatingsForChangeUnit = assessmentRatingsByEntityId.getOrDefault(changeUnitId, emptySet());

                    Set<AssessmentRatingDetail> assessmentRatingDetailForChangeUnit = map(
                            assessmentRatingsForChangeUnit,
                            rating -> mkAssessmentDefinitionDetail(
                                    rating,
                                    ratingSchemeItemsById.get(rating.ratingId())));

                    return ImmutablePhysicalFlowChangeUnitViewItem.builder()
                            .changeUnit(cu)
                            .physicalSpecification(spec)
                            .logicalFlow(logicalFlow)
                            .assessments(assessmentRatingDetailForChangeUnit)
                            .build();
                })
                .collect(toSet());
    }


    private AssessmentRatingDetail mkAssessmentDefinitionDetail(AssessmentRating assessmentRating, RatingSchemeItem ratingDefinition) {
        return ImmutableAssessmentRatingDetail
                .builder()
                .assessmentRating(assessmentRating)
                .ratingDefinition(ratingDefinition)
                .build();
    }
}
