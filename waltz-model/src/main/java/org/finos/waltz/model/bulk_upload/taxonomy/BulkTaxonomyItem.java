package org.finos.waltz.model.bulk_upload.taxonomy;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableBulkTaxonomyItem.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public interface BulkTaxonomyItem {

    String name();

    @JsonAlias({"id", "external_id", "ext_id", "extId"})
    String externalId();

    @JsonAlias({"parent_id", "parent_external_id", "parent_ext_id", "parentExtId"})
    @Nullable
    String parentExternalId();

    @JsonAlias({"desc"})
    @Nullable
    String description();

    @Value.Default
    default boolean concrete() {
        return true;
    }
}
