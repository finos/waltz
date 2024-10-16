package org.finos.waltz.model.bulk_upload.entity_relationship;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUploadRelationshipValidatedItem.class)
public interface BulkUploadRelationshipValidatedItem {
    BulkUploadRelationshipItem parsedItem();

    @Nullable
    EntityReference sourceEntityRef();

    @Nullable
    EntityReference targetEntityRef();

    @Nullable
    String description();

    @Nullable
    Set<ValidationError> error();

    UploadOperation uploadOperation();
}
