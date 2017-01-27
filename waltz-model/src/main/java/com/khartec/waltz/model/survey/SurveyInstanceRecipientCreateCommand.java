package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceCreateCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceCreateCommand.class)
public abstract class SurveyInstanceRecipientCreateCommand {

    public abstract Long surveyInstanceId();
    public abstract Long personId();

}
