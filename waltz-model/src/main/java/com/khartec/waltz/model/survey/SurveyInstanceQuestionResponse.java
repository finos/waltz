package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceQuestionResponse.class)
@JsonDeserialize(as = ImmutableSurveyInstanceQuestionResponse.class)
public abstract class SurveyInstanceQuestionResponse {

    public abstract Long surveyInstanceId();
    public abstract Long personId();
    public abstract LocalDateTime lastUpdatedAt();
    public abstract SurveyQuestionResponse questionResponse();
}
