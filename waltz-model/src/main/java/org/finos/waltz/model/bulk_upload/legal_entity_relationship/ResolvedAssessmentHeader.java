package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.bulk_upload.ResolvedAssessmentHeaderStatus;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableResolvedAssessmentHeader.class)
@JsonDeserialize(as = ImmutableResolvedAssessmentHeader.class)
public abstract class ResolvedAssessmentHeader {

    public abstract String inputString();

    public abstract Optional<AssessmentDefinition> resolvedAssessmentDefinition();

    public abstract Optional<RatingSchemeItem> resolvedRating();  //optionally included in header to allow comment

    public abstract ResolvedAssessmentHeaderStatus status();


    // ----- HELPERS -----

    public static ResolvedAssessmentHeader mkHeader(String inputString,
                                                    Optional<AssessmentDefinition> defn,
                                                    Optional<RatingSchemeItem> rating,
                                                    ResolvedAssessmentHeaderStatus status) {

        return ImmutableResolvedAssessmentHeader.builder()
                .resolvedAssessmentDefinition(defn)
                .resolvedRating(rating)
                .status(status)
                .inputString(inputString)
                .build();
    }


}
