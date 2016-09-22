package com.khartec.waltz.model.tour;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTourStep.class)
@JsonDeserialize(as = ImmutableTourStep.class)
public abstract class TourStep {

    public abstract String key();
    public abstract int id();
    public abstract String selector();
    public abstract String description();
    public abstract String position();

}
