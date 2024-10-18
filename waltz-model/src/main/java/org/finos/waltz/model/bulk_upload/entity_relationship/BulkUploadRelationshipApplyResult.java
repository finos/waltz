package org.finos.waltz.model.bulk_upload.entity_relationship;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadRelationshipApplyResult.class)
public interface BulkUploadRelationshipApplyResult {

    Long recordsAdded();

    Long recordsUpdated();

    Long skippedRows();
}
