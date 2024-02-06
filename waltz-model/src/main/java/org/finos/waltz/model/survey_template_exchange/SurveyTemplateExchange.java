package org.finos.waltz.model.survey_template_exchange;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplateExchange.class)
@JsonDeserialize(as = ImmutableSurveyTemplateExchange.class)
public interface SurveyTemplateExchange {

    SurveyTemplateModel template();
    List<SurveyQuestionModel> questions();

}
