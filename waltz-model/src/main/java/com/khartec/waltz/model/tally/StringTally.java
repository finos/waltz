package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableStringTally.class)
@JsonDeserialize(as = ImmutableStringTally.class)
public abstract class StringTally implements Tally<String> {

}
