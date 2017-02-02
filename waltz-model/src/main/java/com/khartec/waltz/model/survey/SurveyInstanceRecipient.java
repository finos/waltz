package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstanceRecipient.class)
@JsonDeserialize(as = ImmutableSurveyInstanceRecipient.class)
public abstract class SurveyInstanceRecipient implements IdProvider {

    public abstract SurveyInstance surveyInstance();
    public abstract Person person();

}
