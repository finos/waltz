package org.finos.waltz.model.assessment_rating.bulk_upload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkAssessmentRatingApplyResult.class)
public interface BulkAssessmentRatingApplyResult {
    int recordsAdded();
    int recordsUpdated();
    int recordsRemoved();
    int skippedRows();
}
