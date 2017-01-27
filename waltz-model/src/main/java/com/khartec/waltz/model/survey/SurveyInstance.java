package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstance.class)
@JsonDeserialize(as = ImmutableSurveyInstance.class)
public abstract class SurveyInstance implements IdProvider {

    public abstract Long surveyRunId();
    public abstract EntityReference surveyEntity();
    public abstract SurveyInstanceStatus status();
}
