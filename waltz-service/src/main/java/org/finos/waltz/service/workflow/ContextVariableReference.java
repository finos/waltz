package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;


/**
 * This is a friendly entity ref for users to declare in their scripts
 * i.e. ASSESSMENT_DEFINITION, LEGAL_HOLD rather than ASSESSMENT_DEFINITION, 27
 */
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
