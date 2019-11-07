/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.change_unit;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.AssessmentRatingDetail;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentRatingDetail;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.change_unit.ImmutablePhysicalFlowChangeUnitViewItem;
import com.khartec.waltz.model.change_unit.PhysicalFlowChangeUnitViewItem;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.service.assessment_rating.AssessmentRatingService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;


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

        IdSelectionOptions idSelectionOptions = mkOpts(mkRef(EntityKind.CHANGE_SET, changeSetId), HierarchyQueryScope.EXACT);

        Collection<PhysicalFlow> physicalFlows = physicalFlowService.findBySelector(idSelectionOptions);
        Collection<PhysicalSpecification> physicalSpecs = physicalSpecificationService.findByIds(map(physicalFlows, PhysicalFlow::specificationId));
        Collection<LogicalFlow> logicalFlows = logicalFlowService.findAllByFlowIds(map(physicalFlows, PhysicalFlow::logicalFlowId));
        List<AssessmentRating> assessmentRatings = assessmentRatingService.findByTargetKindForRelatedSelector(EntityKind.CHANGE_UNIT, idSelectionOptions);

        Map<Long, RagName> ratingSchemeItemsById = indexBy(ratingSchemeService.getAllRatingSchemeItems(), item -> item.id().get());
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

    private AssessmentRatingDetail mkAssessmentDefinitionDetail(AssessmentRating assessmentRating, RagName ratingDefinition) {
        return ImmutableAssessmentRatingDetail
                .builder()
                .assessmentRating(assessmentRating)
                .ratingDefinition(ratingDefinition)
                .build();
    }
}
