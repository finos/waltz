package org.finos.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable.MeasurableHierarchy;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

/**
 * Represents a set of ratings (with supporting reference data).
 * Often included as part of {@link MeasurableRatingCategoryView#measurableRatings()}
 * which provides more contextual data (allocations, primary ratings etc).
 *
 * However, it is used directly for tasks such as bringing back the set of primary
 * ratings for a specific app (for display in the header).
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingsView.class)
public interface MeasurableRatingsView {

    Set<MeasurableCategory> measurableCategories();
    Set<Measurable> measurables();
    Set<MeasurableRating> measurableRatings();
    Set<RatingSchemeItem> ratingSchemeItems();
    Set<MeasurableHierarchy> measurableHierarchy();

}
