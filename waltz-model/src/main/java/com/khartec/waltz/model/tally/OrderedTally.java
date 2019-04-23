package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOrderedTally.class)
@JsonDeserialize(as = ImmutableOrderedTally.class)
public abstract class OrderedTally<T> extends Tally<T> {

    public abstract int index();
}
