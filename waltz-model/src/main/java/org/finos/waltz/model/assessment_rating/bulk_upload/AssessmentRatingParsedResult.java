package org.finos.waltz.model.assessment_rating.bulk_upload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentRatingParsedResult.class)
public interface AssessmentRatingParsedResult {

    @Value.Immutable
    interface AssessmentRatingParseError {
        String message();

        @Nullable
        Integer line();

        @Nullable
        Integer column();
    }

    List<AssessmentRatingParsedItem> parsedItems();

    @Nullable String input();

    @Nullable
    AssessmentRatingParseError error();


    static AssessmentRatingParsedResult mkResult(List<AssessmentRatingParsedItem> items,
                                                 String input) {
        return ImmutableAssessmentRatingParsedResult
                .builder()
                .parsedItems(items)
                .input(input)
                .build();
    }
}
