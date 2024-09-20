package org.finos.waltz.model.bulk_upload.measurable_rating;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as= ImmutableBulkMeasurableRatingValidationResult.class)
public interface BulkMeasurableRatingValidationResult {

    List<BulkMeasurableRatingValidatedItem> validatedItems();

    @Nullable
    BulkMeasurableRatingParseResult.BulkMeasurableRatingParseError error();

    @Value.Derived
    default int removalCount() {
        return removals().size();
    }

    Set<Tuple2<EntityReference, Long>> removals();

}
