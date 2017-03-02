package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyRunCompletionRate.class)
@JsonDeserialize(as = ImmutableSurveyRunCompletionRate.class)
public abstract class SurveyRunCompletionRate {
    public abstract int notStartedCount();
    public abstract int inProgressCount();
    public abstract int completedCount();
    public abstract int expiredCount();
}
