package org.finos.waltz.model.survey;

import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
public abstract class SurveyInstanceActionQueueItem implements IdProvider {

    public abstract SurveyInstanceAction action();
    public abstract Long surveyInstanceId();
    public abstract Optional<SurveyInstanceActionParams> actionParams();
    public abstract SurveyInstanceStatus initialState();
    public abstract LocalDateTime submittedAt();
    public abstract String submittedBy();
    @Nullable
    public abstract LocalDateTime actionedAt();
    public abstract SurveyInstanceActionStatus status();
    @Nullable
    public abstract String message();
    public abstract String provenance();

}
