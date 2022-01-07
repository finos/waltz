package org.finos.waltz.service.workflow;

import org.finos.waltz.model.EntityKind;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.service.workflow.ContextVariableReference.mkVarRef;

public class ReferenceBuilderNamespace {

    public static String helloWorld(int count, String name) {
        return IntStream.range(0, count)
                .mapToObj(x -> name + "!")
                .collect(Collectors.joining());
    }


    public static ContextVariableReference assessment(String extId) {
        return mkVarRef(EntityKind.ASSESSMENT_DEFINITION, extId);
    }


    public static SurveyResponseContextVariableReference surveyResponse(String surveyTemplateExtId, String questionExtId) {
        return SurveyResponseContextVariableReference.mkVarRef(
                surveyTemplateExtId,
                questionExtId);
    }
}
