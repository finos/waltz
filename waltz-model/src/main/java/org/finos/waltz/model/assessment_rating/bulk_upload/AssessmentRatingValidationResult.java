package org.finos.waltz.model.assessment_rating.bulk_upload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as= ImmutableAssessmentRatingValidationResult.class)
public interface AssessmentRatingValidationResult {
    List<AssessmentRatingValidatedItem> validatedItems();

    @Nullable
    AssessmentRatingParsedResult.AssessmentRatingParseError error();

    @Value.Derived
    default int removalCount() {
        return removals().size();
    }

    Set<Tuple2<EntityReference, Long>> removals();
}
