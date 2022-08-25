package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.complexity.ComplexityKind;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityWidgetData.class)
public abstract class ComplexityWidgetData implements CellDataProvider<ComplexityWidgetDatum> {

    public abstract Set<Application> applications();

    public abstract Set<ComplexityKind> complexityKinds();

}
