package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceResponseCommand.class)
@JsonDeserialize(as = ImmutableSurveyInstanceResponseCommand.class)
public abstract class SurveyInstanceResponseCommand {

    public abstract List<SurveyQuestionResponseChange> questionResponseChanges();
}
