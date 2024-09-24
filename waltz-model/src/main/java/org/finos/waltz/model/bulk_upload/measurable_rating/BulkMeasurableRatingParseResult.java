package org.finos.waltz.model.bulk_upload.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkMeasurableRatingParseResult.class)
public interface BulkMeasurableRatingParseResult {

    @Value.Immutable
    interface BulkMeasurableRatingParseError {
        String message();

        @Nullable
        Integer line();

        @Nullable
        Integer column();
    }

    List<BulkMeasurableRatingItem> parsedItems();

    @Nullable String input();

    @Nullable
    BulkMeasurableRatingParseError error();


    static BulkMeasurableRatingParseResult mkResult(List<BulkMeasurableRatingItem> items,
                                                    String input) {
        return ImmutableBulkMeasurableRatingParseResult
                .builder()
                .parsedItems(items)
                .input(input)
                .build();
    }

}
