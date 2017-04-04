package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceRecipientUpdateCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceRecipientUpdateCommand.class)
public abstract class SurveyInstanceRecipientUpdateCommand {

    public abstract Long instanceRecipientId();
    public abstract Long surveyInstanceId();
    public abstract Long personId();

}
