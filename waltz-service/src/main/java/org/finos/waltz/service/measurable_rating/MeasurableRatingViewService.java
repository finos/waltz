package org.finos.waltz.service.measurable_rating;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingAppView;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.ImmutableMeasurableRatingView;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingAppView;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.MeasurableRatingView;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.allocation.AllocationService;
import org.finos.waltz.service.allocation_schemes.AllocationSchemeService;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.service.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionService;
import org.finos.waltz.service.measurable_rating_replacement.MeasurableRatingReplacementService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class MeasurableRatingViewService {

    private final MeasurableRatingService measurableRatingService;
    private final MeasurableService measurableService;
    private final MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService;
    private final MeasurableRatingReplacementService measurableRatingReplacementService;
    private final RatingSchemeService ratingSchemeService;

    private final MeasurableCategoryService measurableCategoryService;

    private final AssessmentRatingService assessmentRatingService;

    private final AssessmentDefinitionService assessmentDefinitionService;

    private final AllocationService allocationService;
    private final AllocationSchemeService allocationSchemeService;

    public MeasurableRatingViewService(MeasurableRatingService measurableRatingService,
                                       MeasurableService measurableService,
                                       MeasurableRatingPlannedDecommissionService measurableRatingPlannedDecommissionService,
                                       MeasurableRatingReplacementService measurableRatingReplacementService,
                                       RatingSchemeService ratingSchemeService,
                                       MeasurableCategoryService measurableCategoryService,
                                       AssessmentRatingService assessmentRatingService,
                                       AssessmentDefinitionService assessmentDefinitionService,
                                       AllocationService allocationService,
                                       AllocationSchemeService allocationSchemeService){

        this.measurableRatingService = measurableRatingService;
        this.measurableService = measurableService;
        this.measurableRatingPlannedDecommissionService = measurableRatingPlannedDecommissionService;
        this.measurableRatingReplacementService = measurableRatingReplacementService;
        this.ratingSchemeService = ratingSchemeService;
        this.measurableCategoryService = measurableCategoryService;
        this.assessmentRatingService = assessmentRatingService;
        this.assessmentDefinitionService = assessmentDefinitionService;
        this.allocationService = allocationService;
        this.allocationSchemeService = allocationSchemeService;
    }

    public MeasurableRatingView getViewById(long id) {

        MeasurableRating measurableRating = measurableRatingService.getById(id);

        Measurable measurable = measurableService.getById(measurableRating.measurableId());

        if (measurable == null) {

            return ImmutableMeasurableRatingView.builder()
                    .measurableRating(measurableRating)
                    .measurable(null)
                    .rating(null)
                    .decommission(null)
                    .replacements(emptyList())
                    .build();
        } else {

            EntityReference measurableRef = measurable.entityReference();

            List<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(measurableRating.entityReference(), measurable.categoryId());

            Map<String, RatingSchemeItem> itemsByCode = indexBy(ratingSchemeItems, RatingSchemeItem::rating);
            RatingSchemeItem rating = itemsByCode.get(String.valueOf(measurableRating.rating()));

            MeasurableRatingPlannedDecommission decomm = measurableRatingPlannedDecommissionService.getByMeasurableRatingId(id);

            Set<MeasurableRatingReplacement> replacementApps = decomm == null
                    ? Collections.emptySet()
                    : measurableRatingReplacementService.getByDecommId(decomm.id());

            List<AllocationScheme> schemes = allocationSchemeService.findByCategoryId(measurable.categoryId());

            Set<Allocation> allocations = allocationService.findByMeasurableRatingId(id);

            return ImmutableMeasurableRatingView.builder()
                    .measurableRating(measurableRating)
                    .measurable(measurableRef)
                    .rating(rating)
                    .decommission(decomm)
                    .replacements(replacementApps)
                    .allocations(allocations)
                    .allocationSchemes(schemes)
                    .build();
        }

    }

    public MeasurableRatingAppView getViewForApp(EntityReference ref) {

        List<MeasurableRating> ratings = measurableRatingService.findForEntity(ref);
        List<Measurable> measurables = measurableService.findAll();
        Collection<MeasurableCategory> categories = measurableCategoryService.findAll();
        List<AllocationScheme> allocSchemes = allocationSchemeService.findAll();
        Collection<Allocation> allocs = allocationService.findByEntity(ref);
        Set<AssessmentDefinition> defs = assessmentDefinitionService.findByEntityKind(EntityKind.MEASURABLE_RATING);
        List<AssessmentRating> assessments = assessmentRatingService.findByEntityKind(EntityKind.MEASURABLE_RATING);
        Collection<RatingScheme> ratingSchemes = ratingSchemeService.findAll();
        Collection<MeasurableRatingPlannedDecommission> decomms = measurableRatingPlannedDecommissionService.findForEntityRef(ref);
        Collection<MeasurableRatingReplacement> replacements = measurableRatingReplacementService.findForEntityRef(ref);

        return ImmutableMeasurableRatingAppView.builder()
                .measurableRatings(ratings)
                .measurables(measurables)
                .categories(categories)
                .allocationSchemes(allocSchemes)
                .allocations(allocs)
                .plannedDecommissions(decomms)
                .plannedReplacements(replacements)
                .assessmentDefinitions(defs)
                .assessmentRatings(assessments)
                .ratingSchemes(ratingSchemes)
                .build();
    }

    public MeasurableRatingCategoryView getViewForAppAndCategory(EntityReference ref, long categoryId) {

        MeasurableCategory category = measurableCategoryService.getById(categoryId);
        List<MeasurableRating> ratings = measurableRatingService.findForEntityAndCategory(ref, categoryId);
        List<Measurable> measurables = measurableService.findByCategoryId(categoryId);
        List<AllocationScheme> allocSchemes = allocationSchemeService.findByCategoryId(categoryId);
        Collection<Allocation> allocs = allocationService.findByEntityAndCategory(ref, categoryId);

        Set<AssessmentDefinition> defs = SetUtilities.filter(
                assessmentDefinitionService.findByEntityKind(EntityKind.MEASURABLE_RATING),
                d -> d.qualifierReference()
                        .map(qualifierRef -> qualifierRef.id() == categoryId)
                        .orElse(false));

        List<AssessmentRating> assessments = assessmentRatingService.findByEntityKind(EntityKind.MEASURABLE_RATING);
        Collection<RatingScheme> ratingSchemes = ratingSchemeService.findAll();
        Collection<MeasurableRatingPlannedDecommission> decomms = measurableRatingPlannedDecommissionService.findForEntityRefAndCategory(ref, categoryId);
        Collection<MeasurableRatingReplacement> replacements = measurableRatingReplacementService.fetchByEntityRefAndCategory(ref, categoryId);

        return ImmutableMeasurableRatingCategoryView.builder()
                .ratings(ratings)
                .measurables(measurables)
                .category(category)
                .allocationSchemes(allocSchemes)
                .allocations(allocs)
                .plannedDecommissions(decomms)
                .plannedReplacements(replacements)
                .assessmentDefinitions(defs)
                .assessmentRatings(assessments)
                .ratingSchemes(ratingSchemes)
                .build();
    }
}
