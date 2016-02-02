package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableIdGroup.class)
@JsonDeserialize(as = ImmutableIdGroup.class)
public abstract class IdGroup extends KeyedGroup<Long, Long> {
}
