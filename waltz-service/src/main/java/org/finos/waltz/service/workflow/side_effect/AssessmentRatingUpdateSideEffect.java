package org.finos.waltz.service.workflow.side_effect;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AssessmentRatingUpdateSideEffect {

    public abstract String assessmentDefinitionExternalId();
    public abstract String ratingSchemeItemExternalId();

}
