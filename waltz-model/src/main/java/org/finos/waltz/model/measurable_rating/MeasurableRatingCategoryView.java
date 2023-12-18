package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingCategoryView.class)
public abstract class MeasurableRatingCategoryView {

//    {
//        category,
//                ratingScheme,
//                measurables,
//                ratings: ratingsForCategory,
//            allocationSchemes: schemes,
//            allocations: ctx.allocations,
//            assessmentDefinitions: definitionsForCategory,
//            assessmentRatings: assessmentRatingsForCategory,
//            plannedDecommissions: decommsForCategory,
//            plannedReplacements: replacementsForCategory
//    }

    public abstract MeasurableCategory category();

    public abstract Set<RatingScheme> ratingSchemes();

    public abstract Set<Measurable> measurables();

    public abstract Set<MeasurableRating> ratings();

    public abstract Set<AllocationScheme> allocationSchemes();

    public abstract Set<Allocation> allocations();

    public abstract Set<AssessmentDefinition> assessmentDefinitions();

    public abstract Set<AssessmentRating> assessmentRatings();

    public abstract Set<MeasurableRatingPlannedDecommission> plannedDecommissions();

    public abstract Set<MeasurableRatingReplacement> plannedReplacements();


    @Value.Derived
    public Map<Long, RatingSchemeItem> ratingSchemeItemsById() {
        return ratingSchemes()
                .stream()
                .flatMap(d -> d.ratings().stream())
                .collect(toMap(
                        d -> d.id().get(),
                        d -> d));
    }

    @Value.Derived
    public Map<String, RatingSchemeItem> ratingSchemeItemsByCode() {
        return ratingSchemes()
                .stream()
                .filter(d -> category().ratingSchemeId() == d.id().get())
                .flatMap(d -> d.ratings().stream())
                .collect(toMap(
                        RatingSchemeItem::rating,
                        d -> d));
    }
}
