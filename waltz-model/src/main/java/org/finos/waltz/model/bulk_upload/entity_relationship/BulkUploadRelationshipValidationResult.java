package org.finos.waltz.model.bulk_upload.entity_relationship;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadRelationshipValidationResult.class)
public interface BulkUploadRelationshipValidationResult {
    List<BulkUploadRelationshipValidatedItem> validatedItems();

    @Nullable
    BulkUploadRelationshipParsedResult.BulkUploadRelationshipParseError parseError();
}
