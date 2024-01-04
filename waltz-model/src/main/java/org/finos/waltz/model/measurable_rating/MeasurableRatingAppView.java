package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingAppView.class)
public abstract class MeasurableRatingAppView {

    public abstract Set<MeasurableRating> measurableRatings();

    public abstract Set<Measurable> measurables();

    public abstract Set<RatingScheme> ratingSchemes();

    public abstract Set<MeasurableCategory> categories();

    public abstract Set<MeasurableRatingPlannedDecommission> plannedDecommissions();

    public abstract Set<MeasurableRatingReplacement> plannedReplacements();

    public abstract Set<Allocation> allocations();

    public abstract Set<AllocationScheme> allocationSchemes();

    public abstract Set<AssessmentDefinition> assessmentDefinitions();

    public abstract Set<AssessmentRating> assessmentRatings();

}
