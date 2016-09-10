package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntry.class)
@JsonDeserialize(as = ImmutableEntry.class)
public abstract class Entry<K,V> {
    public abstract K key();
    public abstract V value();
}