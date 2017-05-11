package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstance.class)
@JsonDeserialize(as = ImmutableSurveyInstance.class)
public abstract class SurveyInstance implements IdProvider {

    public abstract Long surveyRunId();
    public abstract EntityReference surveyEntity();
    public abstract SurveyInstanceStatus status();
    public abstract LocalDate dueDate();

    @Nullable
    public abstract LocalDateTime submittedAt();

    @Nullable
    public abstract String submittedBy();
}
