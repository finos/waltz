package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.service.workflow.WorkflowContextVariableReference.mkVarRef;

public class ReferenceBuilderNamespace {

    public static String helloWorld(int count, String name) {
        return IntStream.range(0, count)
                .mapToObj(x -> name + "!")
                .collect(Collectors.joining());
    }


    public static WorkflowContextVariableReference assessment(String extId) {
        return mkVarRef(EntityKind.ASSESSMENT_DEFINITION, extId);
    }


    public static WorkflowContextSurveyResponseVariableReference surveyResponse(String surveyTemplateExtId, String questionExtId) {
        return WorkflowContextSurveyResponseVariableReference.mkVarRef(
                surveyTemplateExtId,
                questionExtId);
    }
}
