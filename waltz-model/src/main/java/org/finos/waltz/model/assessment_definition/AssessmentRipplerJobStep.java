package org.finos.waltz.model.assessment_definition;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableAssessmentRipplerJobStep.class)
@JsonSerialize(as = ImmutableAssessmentRipplerJobStep.class)
public interface AssessmentRipplerJobStep {

    @JsonAlias({"from", "from_def"})
    String fromDef();

    @JsonAlias({"to", "to_def"})
    String toDef();

}
