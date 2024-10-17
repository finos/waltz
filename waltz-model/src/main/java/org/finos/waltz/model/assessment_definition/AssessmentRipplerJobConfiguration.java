package org.finos.waltz.model.assessment_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableAssessmentRipplerJobConfiguration.class)
@JsonSerialize(as = ImmutableAssessmentRipplerJobConfiguration.class)
public interface AssessmentRipplerJobConfiguration {

    String name();

    List<AssessmentRipplerJobStep> steps();

}
