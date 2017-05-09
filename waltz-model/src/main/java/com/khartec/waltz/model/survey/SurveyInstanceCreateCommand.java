package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceCreateCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceCreateCommand.class)
public abstract class SurveyInstanceCreateCommand  {

    public abstract Long surveyRunId();
    public abstract EntityReference entityReference();
    public abstract Optional<LocalDate> dueDate();


    @Value.Default
    public SurveyInstanceStatus status() {
        return SurveyInstanceStatus.NOT_STARTED;
    }
}
