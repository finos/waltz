package org.finos.waltz.model.bulk_upload.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkMeasurableRatingApplyResult.class)
public interface BulkMeasurableRatingApplyResult {

    int recordsAdded();
    int recordsUpdated();
    int recordsRemoved();

    int skippedRows();

}
