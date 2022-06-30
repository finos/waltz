package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.cost.EntityCostKind;
import org.finos.waltz.model.measurable.Measurable;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableCostWidgetData.class)
public abstract class CostWidgetData implements CellDataProvider<CostWidgetDatum> {


    public abstract Set<Application> applications();

    public abstract Set<Measurable> measurables();

    public abstract Set<EntityCostKind> costKinds();

}
