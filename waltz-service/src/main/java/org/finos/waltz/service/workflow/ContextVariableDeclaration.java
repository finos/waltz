package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
public abstract class ContextVariableDeclaration {

    public abstract String name();
    public abstract ContextVariableReference ref();


    public static ContextVariableDeclaration mkDecl(String nicename, EntityKind kind, String extId) {
        return mkDecl(nicename, ContextVariableReference.mkVarRef(kind, extId));
    }


    public static ContextVariableDeclaration mkDecl(String nicename, ContextVariableReference ref) {
        return ImmutableContextVariableDeclaration
                .builder()
                .name(nicename)
                .ref(ref)
                .build();
    }

}
