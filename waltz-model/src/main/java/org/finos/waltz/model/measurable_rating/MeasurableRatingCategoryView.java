package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.AssessmentsView;
import org.finos.waltz.model.application.MeasurableRatingsView;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable.MeasurableHierarchy;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionInfo;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingCategoryView.class)
public interface MeasurableRatingCategoryView {

    Set<Application> applications();

    AllocationsView allocations();

    AssessmentsView primaryAssessments();

    DecommissionsView decommissions();

    MeasurableRatingsView measurableRatings();
}
