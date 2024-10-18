package org.finos.waltz.model.bulk_upload.entity_relationship;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadRelationshipParsedResult.class)
public interface BulkUploadRelationshipParsedResult {

    @Value.Immutable
    interface BulkUploadRelationshipParseError {
        String message();

        @Nullable
        Integer line();

        @Nullable
        Integer column();
    }

    String input();

    List<BulkUploadRelationshipItem> parsedItems();

    @Nullable
    BulkUploadRelationshipParseError error();

    static BulkUploadRelationshipParsedResult mkResult(List<BulkUploadRelationshipItem> items,
                                                    String input) {
        if(items.isEmpty()) {
            return ImmutableBulkUploadRelationshipParsedResult
                    .builder()
                    .input(input)
                    .error(ImmutableBulkUploadRelationshipParseError
                            .builder()
                            .message("Cannot parse input.")
                            .build())
                    .build();
        }

        return ImmutableBulkUploadRelationshipParsedResult
                .builder()
                .parsedItems(items)
                .input(input)
                .build();
    }
}
