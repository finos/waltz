package org.finos.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWithOperations.class)
public abstract class EntityWithOperations<T> {

    public abstract T entity();
    public abstract Set<Operation> operations();
}
