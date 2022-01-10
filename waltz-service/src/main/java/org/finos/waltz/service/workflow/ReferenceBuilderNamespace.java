package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;

import static org.finos.waltz.service.workflow.ContextVariableReference.mkVarRef;
import static org.finos.waltz.service.workflow.SurveyQuestionResponseContextVariableReference.mkVarRef;

public class ReferenceBuilderNamespace {


    public static ContextVariableReference assessment(String extId) {
        return mkVarRef(EntityKind.ASSESSMENT_DEFINITION, extId);
    }


    public static SurveyQuestionResponseContextVariableReference surveyQuestionResponse(String surveyTemplateExtId,
                                                                                        String questionExtId)
    {
        return mkVarRef(
                surveyTemplateExtId,
                questionExtId);
    }
}
