package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplateStatusChangeCommand.class)
@JsonDeserialize(as = ImmutableSurveyTemplateStatusChangeCommand.class)
public abstract class SurveyTemplateStatusChangeCommand {

    public abstract SurveyTemplateStatus newStatus();
}
