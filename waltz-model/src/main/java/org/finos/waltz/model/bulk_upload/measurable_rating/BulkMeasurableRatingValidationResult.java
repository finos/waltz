package org.finos.waltz.model.bulk_upload.measurable_rating;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.measurable.Measurable;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as= ImmutableBulkMeasurableRatingValidationResult.class)
public interface BulkMeasurableRatingValidationResult {

    List<BulkMeasurableRatingValidatedItem> validatedItems();

}
