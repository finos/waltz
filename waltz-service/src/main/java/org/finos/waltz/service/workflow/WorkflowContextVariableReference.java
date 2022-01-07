package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
public abstract class WorkflowContextVariableReference  {

    public abstract EntityKind kind();
    public abstract String externalId();


    public static WorkflowContextVariableReference mkVarRef(EntityKind kind,
                                                            String externalId) {
        return ImmutableWorkflowContextVariableReference
                .builder()
                .kind(kind)
                .externalId(externalId)
                .build();
    }

}
