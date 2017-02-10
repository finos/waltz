package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceStatusChangeCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceStatusChangeCommand.class)
public abstract class SurveyInstanceStatusChangeCommand {

    public abstract SurveyInstanceStatus newStatus();
}
