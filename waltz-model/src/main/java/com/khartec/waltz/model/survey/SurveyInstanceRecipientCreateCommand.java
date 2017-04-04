package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceRecipientCreateCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceRecipientCreateCommand.class)
public abstract class SurveyInstanceRecipientCreateCommand {

    public abstract Long surveyInstanceId();
    public abstract Long personId();

}
