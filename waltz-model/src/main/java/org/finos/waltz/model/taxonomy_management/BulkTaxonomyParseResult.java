package org.finos.waltz.model.taxonomy_management;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkTaxonomyParseResult.class)
public interface BulkTaxonomyParseResult {

    @Value.Immutable
    interface BulkTaxonomyParseError {
        String message();

        @Nullable
        Integer line();

        @Nullable
        Integer column();
    }

    List<BulkTaxonomyItem> parsedItems();

    @Nullable String input();

    @Nullable
    BulkTaxonomyParseError error();


    static BulkTaxonomyParseResult mkResult(List<BulkTaxonomyItem> items,
                                            String input) {
        return ImmutableBulkTaxonomyParseResult
                .builder()
                .parsedItems(items)
                .input(input)
                .build();
    }

}
