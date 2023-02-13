package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRatingResolutionError.class)
@JsonDeserialize(as = ImmutableRatingResolutionError.class)
public abstract class RatingResolutionError {


    public abstract AssessmentResolutionErrorCode errorCode();

    public abstract String errorMessage();

}
