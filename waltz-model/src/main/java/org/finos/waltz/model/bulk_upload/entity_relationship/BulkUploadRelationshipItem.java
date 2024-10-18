package org.finos.waltz.model.bulk_upload.entity_relationship;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableBulkUploadRelationshipItem.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public interface BulkUploadRelationshipItem {

    @JsonAlias({"source_external_id", "source_id"})
    String sourceExternalId();

    @JsonAlias({"target_external_id", "target_id"})
    String targetExternalId();

    @Nullable
    String description();

}
