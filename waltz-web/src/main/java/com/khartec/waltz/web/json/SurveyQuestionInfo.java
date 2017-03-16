package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionDropdownEntry;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestionInfo.class)
@JsonDeserialize(as = ImmutableSurveyQuestionInfo.class)
public abstract class SurveyQuestionInfo {

    public abstract SurveyQuestion question();
    public abstract List<SurveyQuestionDropdownEntry> dropdownEntries();
}
