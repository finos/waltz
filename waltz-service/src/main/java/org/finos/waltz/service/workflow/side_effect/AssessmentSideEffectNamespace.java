package org.finos.waltz.service.workflow.side_effect;

public class AssessmentSideEffectNamespace {

    public static ImmutableAssessmentRatingUpdateSideEffect update(String defnExtId, String ratingExtId) {
        return ImmutableAssessmentRatingUpdateSideEffect.builder()
                .assessmentDefinitionExternalId(defnExtId)
                .ratingSchemeItemExternalId(ratingExtId)
                .build();
    }

}
