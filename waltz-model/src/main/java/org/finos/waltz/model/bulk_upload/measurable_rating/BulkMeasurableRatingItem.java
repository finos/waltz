package org.finos.waltz.model.bulk_upload.measurable_rating;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableBulkMeasurableRatingItem.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public interface BulkMeasurableRatingItem {

    @JsonAlias({"nar_id", "application_id", "asset_code"})
    String assetCode();

    @JsonAlias({"taxonomy_external_id", "external_id", "ext_id", "extId"})
    String taxonomyExternalId();

    char ratingCode();

    @Value.Default
    default boolean isPrimary() { return false; }

    @Nullable
    String comment();

    @Nullable
    Integer allocation();

    @JsonAlias({"scheme_external_id", "allocation_scheme_external_id", "allocation_scheme"})
    String scheme();
}
