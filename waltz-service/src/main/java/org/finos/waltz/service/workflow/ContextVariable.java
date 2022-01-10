package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ContextVariable<T> {
    public abstract EntityReference entityRef();
    public abstract String name();
    public abstract T value();
}
