package org.finos.waltz.model.survey_template_exchange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyDropdownEntryModel.class)
@JsonDeserialize(as = ImmutableSurveyDropdownEntryModel.class)
public interface SurveyDropdownEntryModel {

    int position();
    String value();
}
