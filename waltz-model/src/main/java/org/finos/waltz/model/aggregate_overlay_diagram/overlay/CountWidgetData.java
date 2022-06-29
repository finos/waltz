package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as= ImmutableCountWidgetData.class)
public abstract class CountWidgetData implements CellDataProvider<CountWidgetDatum> {


}
