package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentWidgetParameters.class)
@JsonDeserialize(as = ImmutableAssessmentWidgetParameters.class)
public abstract class AssessmentWidgetParameters {

    public abstract Long assessmentDefinitionId();

    public abstract Optional<LocalDate> targetDate();

}
