package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
public abstract class SurveyResponseContextVariableReference extends ContextVariableReference {

    public abstract String surveyExternalId();


    public static SurveyResponseContextVariableReference mkVarRef(String surveyExternalId, String externalId) {
        return ImmutableSurveyResponseContextVariableReference
                .builder()
                .kind(EntityKind.SURVEY_QUESTION)
                .surveyExternalId(surveyExternalId)
                .externalId(externalId)
                .build();
    }

}
