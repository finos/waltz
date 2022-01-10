package org.finos.waltz.service.workflow;

import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class AssessmentContextValue implements ContextValue {

    public abstract String ratingCode();
    public abstract String ratingName();

    @Nullable
    public abstract String ratingExternalId();

    @Nullable
    public abstract String ratingComment();

}
