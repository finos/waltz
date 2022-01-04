package org.finos.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceFormDetails.class)
@JsonDeserialize(as = ImmutableSurveyInstanceFormDetails.class)
public abstract class SurveyInstanceFormDetails {

    public abstract List<SurveyQuestion> activeQs();

    public abstract Set<Long> missingMandatoryQuestions();

}
