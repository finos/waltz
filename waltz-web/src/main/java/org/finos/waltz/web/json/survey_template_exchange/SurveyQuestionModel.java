package org.finos.waltz.web.json.survey_template_exchange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestionModel.class)
@JsonDeserialize(as = ImmutableSurveyQuestionModel.class)
public interface SurveyQuestionModel extends ExternalIdProvider {

    String questionText();
    SurveyQuestionFieldType fieldType();
    int position();
    boolean isMandatory();
    boolean allowComment();

    List<SurveyDropdownEntryModel> dropdownEntries();

    @Nullable
    EntityReference qualifierEntity();

    @Nullable
    String helpText();

    @Nullable
    String label();

    @Nullable
    String parentExternalId();

    @Nullable
    String sectionName();

    @Nullable
    String inclusionPredicate();

}
