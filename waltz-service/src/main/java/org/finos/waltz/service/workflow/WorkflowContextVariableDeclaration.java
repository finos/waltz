package org.finos.waltz.service.workflow;

import org.immutables.value.Value;

@Value.Immutable
public abstract class WorkflowContextVariableDeclaration {

    public abstract String name();
    public abstract WorkflowContextVariableReference ref();

}
