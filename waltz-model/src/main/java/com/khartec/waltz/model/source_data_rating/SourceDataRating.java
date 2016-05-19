package com.khartec.waltz.model.source_data_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.capabilityrating.RagRating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSourceDataRating.class)
@JsonDeserialize(as = ImmutableSourceDataRating.class)
public abstract class SourceDataRating {

    public abstract String sourceName();
    public abstract EntityKind entityKind();

    public abstract RagRating authoritativeness();
    public abstract RagRating accuracy();
    public abstract RagRating completeness();
}
