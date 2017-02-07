package com.khartec.waltz.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableIdCommandResponse.class)
@JsonDeserialize(as = ImmutableIdCommandResponse.class)
public abstract class IdCommandResponse implements IdProvider {
}
