package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
public abstract class WorkflowContextSurveyResponseVariableReference extends WorkflowContextVariableReference {

    public abstract String surveyExternalId();


    public static WorkflowContextSurveyResponseVariableReference mkVarRef(String surveyExternalId, String externalId) {
        return ImmutableWorkflowContextSurveyResponseVariableReference
                .builder()
                .kind(EntityKind.SURVEY_QUESTION)
                .surveyExternalId(surveyExternalId)
                .externalId(externalId)
                .build();
    }

}
