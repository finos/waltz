package org.finos.waltz.service.workflow;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ContextVariableDeclaration {

    public abstract String name();
    public abstract ContextVariableReference ref();

}
