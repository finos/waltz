package org.finos.waltz.model.bulk_upload.taxonomy;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableBulkTaxonomyValidationResult.class)
public interface BulkTaxonomyValidationResult {

    List<BulkTaxonomyValidatedItem> validatedItems();
    int plannedRemovalCount();

}
