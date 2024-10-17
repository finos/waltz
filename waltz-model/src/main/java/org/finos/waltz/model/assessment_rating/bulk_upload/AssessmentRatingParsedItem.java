package org.finos.waltz.model.assessment_rating.bulk_upload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableAssessmentRatingParsedItem.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public interface AssessmentRatingParsedItem {
    @JsonAlias("external_id")
    String externalId();

    String ratingCode();

    @Value.Default
    default boolean isReadOnly() { return false; }

    @Nullable
    String comment();
}
