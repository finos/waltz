package org.finos.waltz.model.bulk_upload.legal_entity_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableResolvedUploadRow.class)
@JsonDeserialize(as = ImmutableResolvedUploadRow.class)
public abstract class ResolvedUploadRow {

    public abstract long rowNumber();

    public abstract ResolvedLegalEntityRelationship legalEntityRelationship();

    public abstract Set<ResolvedAssessmentRating> assessmentRatings();

}
