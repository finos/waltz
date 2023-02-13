package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentHeaderResolutionError.class)
@JsonDeserialize(as = ImmutableAssessmentHeaderResolutionError.class)
public abstract class AssessmentHeaderResolutionError {


    public abstract AssessmentResolutionErrorCode errorCode();

    public abstract String errorMessage();

    public static AssessmentHeaderResolutionError mkError(AssessmentResolutionErrorCode code, String message) {
        return ImmutableAssessmentHeaderResolutionError.builder()
                .errorCode(code)
                .errorMessage(message)
                .build();
    }

}
