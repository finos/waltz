package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Collection;

/**
 * Simple container class to hold producers and consumers
 * @param <X>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableProduceConsumeGroup.class)
@JsonDeserialize(as = ImmutableProduceConsumeGroup.class)
public abstract class ProduceConsumeGroup<X> {

    public abstract Collection<X> produces();
    public abstract Collection<X> consumes();

}
