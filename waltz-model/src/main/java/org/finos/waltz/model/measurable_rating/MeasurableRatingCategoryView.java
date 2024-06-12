package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.AssessmentsView;
import org.finos.waltz.model.application.MeasurableRatingsView;
import org.immutables.value.Value;

import java.util.Set;

/**
 * Aggregate object which provides all the information to draw the
 * measurable rating tree for group and entity viewpoints.
 *
 * Note: the measurable ratings view (simple table + pie) uses ratings
 * directly.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingCategoryView.class)
public interface MeasurableRatingCategoryView {

    Set<Application> applications();

    AllocationsView allocations();

    AssessmentsView primaryAssessments();

    DecommissionsView decommissions();

    MeasurableRatingsView measurableRatings();

    MeasurableRatingsView primaryRatings();
}
