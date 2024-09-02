package org.finos.waltz.model.bulk_upload.taxonomy;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as=ImmutableBulkTaxonomyValidatedItem.class)
public interface BulkTaxonomyValidatedItem {

    BulkTaxonomyItem parsedItem();
    ChangeOperation changeOperation();
    Set<ChangedFieldType> changedFields();
    Set<ValidationError> errors();
}