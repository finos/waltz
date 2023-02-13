package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableResolvedAssessmentHeader.class)
@JsonDeserialize(as = ImmutableResolvedAssessmentHeader.class)
public abstract class ResolvedAssessmentHeader {

    public abstract Optional<AssessmentDefinition> headerDefinition();

    public abstract Optional<RatingSchemeItem> headerRating();  //optionally included in header to allow comment

    public abstract Set<AssessmentHeaderResolutionError> errors();

    public abstract ResolutionStatus status();


    public static ResolvedAssessmentHeader mkHeader(Optional<AssessmentDefinition> defn,
                                                    Optional<RatingSchemeItem> rating,
                                                    Set<AssessmentHeaderResolutionError> errors,
                                                    ResolutionStatus status) {

        return ImmutableResolvedAssessmentHeader.builder()
                .headerDefinition(defn)
                .headerRating(rating)
                .errors(errors)
                .status(status)
                .build();
    }


}
