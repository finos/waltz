package org.finos.waltz.service.workflow;

import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class SurveyQuestionResponseContextValue implements ContextValue {

    public abstract String value();

    @Nullable
    public abstract String comment();

}
