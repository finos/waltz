package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestionDropdownEntry.class)
@JsonDeserialize(as = ImmutableSurveyQuestionDropdownEntry.class)
public abstract class SurveyQuestionDropdownEntry implements IdProvider {

    public abstract Optional<Long> questionId();
    public abstract String value();
    public abstract int position();
}
