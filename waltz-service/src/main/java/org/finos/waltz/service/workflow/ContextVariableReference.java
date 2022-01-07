package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ContextVariableReference {

    public abstract EntityKind kind();
    public abstract String externalId();


    public static ContextVariableReference mkVarRef(EntityKind kind,
                                                    String externalId) {
        return ImmutableContextVariableReference
                .builder()
                .kind(kind)
                .externalId(externalId)
                .build();
    }

}
