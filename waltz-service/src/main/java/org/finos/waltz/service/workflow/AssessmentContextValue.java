package org.finos.waltz.service.workflow;

import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class AssessmentContextValue implements ContextValue {

    public abstract String getRatingCode();
    public abstract String getRatingName();

    @Nullable
    public abstract String getRatingExternalId();

    @Nullable
    public abstract String getRatingComment();

}
