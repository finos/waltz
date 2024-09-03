package org.finos.waltz.model.bulk_upload.taxonomy;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.measurable.Measurable;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as=ImmutableBulkTaxonomyValidationResult.class)
public interface BulkTaxonomyValidationResult {

    List<BulkTaxonomyValidatedItem> validatedItems();

    Set<Measurable> plannedRemovals();

}
