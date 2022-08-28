package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.application.Application;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as= ImmutableAttestationWidgetData.class)
public abstract class AttestationWidgetData implements CellDataProvider<AttestationWidgetDatum> {

    public abstract Set<Application> applications();

}
