package org.finos.waltz.model.bulk_upload.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as= ImmutableBulkMeasurableRatingValidatedItem.class)
public interface BulkMeasurableRatingValidatedItem {

    BulkMeasurableRatingItem parsedItem();

    ChangeOperation changeOperation();

    Set<ChangedFieldType> changedFields();

    Set<ValidationError> errors();

    @Nullable
    Application application();

    @Nullable
    Measurable measurable();

    @Nullable
    RatingSchemeItem ratingSchemeItem();
}