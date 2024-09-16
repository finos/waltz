package org.finos.waltz.model.bulk_upload.taxonomy;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkTaxonomyApplyResult.class)
public interface BulkTaxonomyApplyResult {
    int recordsAdded();

    int recordsUpdated();

    int recordsRemoved();

    int recordsRestored();

    boolean hierarchyRebuilt();
}
