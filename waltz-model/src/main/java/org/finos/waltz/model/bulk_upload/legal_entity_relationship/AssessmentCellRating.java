package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.bulk_upload.ResolutionStatus;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentCellRating.class)
@JsonDeserialize(as = ImmutableAssessmentCellRating.class)
public abstract class AssessmentCellRating {

    public abstract Optional<RatingSchemeItem> resolvedRating(); //can be provided via header rating or listed in cell

    public abstract Optional<String> comment(); //can be provided if headerRatingId is present

    public abstract ResolutionStatus status();

    public abstract Set<RatingResolutionError> errors();

}
