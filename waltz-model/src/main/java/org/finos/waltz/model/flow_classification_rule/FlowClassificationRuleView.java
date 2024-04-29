package org.finos.waltz.model.flow_classification_rule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.flow_classification.FlowClassification;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowClassificationRuleView.class)
public abstract class FlowClassificationRuleView {

    public abstract Set<FlowClassificationRule> flowClassificationRules();

    public abstract Set<AssessmentRating> assessmentRatings();

    public abstract Set<AssessmentDefinition> primaryAssessmentDefinitions();

    public abstract Set<RatingSchemeItem> ratingSchemeItems();

    public abstract Set<DataType> dataTypes();

    public abstract Set<FlowClassification> flowClassifications();

}
