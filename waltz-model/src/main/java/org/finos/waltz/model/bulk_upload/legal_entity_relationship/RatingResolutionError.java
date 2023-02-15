package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRatingResolutionError.class)
@JsonDeserialize(as = ImmutableRatingResolutionError.class)
public abstract class RatingResolutionError implements BulkUploadError {

    public abstract RatingResolutionErrorCode errorCode();

    public static RatingResolutionError mkError(RatingResolutionErrorCode code, String message) {
        return ImmutableRatingResolutionError.builder()
                .errorCode(code)
                .errorMessage(message)
                .build();
    }
}
