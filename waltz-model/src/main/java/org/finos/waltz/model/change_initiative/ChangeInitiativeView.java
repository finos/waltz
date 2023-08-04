package org.finos.waltz.model.change_initiative;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as=ImmutableChangeInitiativeView.class)
public abstract class ChangeInitiativeView {

    public abstract Set<ChangeInitiative> changeInitiatives();
    public abstract Set<AssessmentDefinition> assessmentDefinitions();
    public abstract Set<RatingSchemeItem> ratingSchemeItems();
    public abstract Set<AssessmentRating> ratings();

}
