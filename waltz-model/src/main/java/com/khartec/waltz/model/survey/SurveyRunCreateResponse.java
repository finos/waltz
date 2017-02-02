package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyRunCreateResponse.class)
@JsonDeserialize(as = ImmutableSurveyRunCreateResponse.class)
public abstract class SurveyRunCreateResponse implements IdProvider {
}
