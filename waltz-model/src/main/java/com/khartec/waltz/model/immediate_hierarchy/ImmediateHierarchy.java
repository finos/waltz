package com.khartec.waltz.model.immediate_hierarchy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableImmediateHierarchy.class)
@JsonDeserialize(as = ImmutableImmediateHierarchy.class)
public abstract class ImmediateHierarchy<T> {

    public abstract T self();
    public abstract Optional<T> parent();
    public abstract List<T> siblings();
    public abstract List<T> children();
}
