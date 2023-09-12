package org.finos.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAllocatedCostDefinition.class)
@JsonDeserialize(as = ImmutableAllocatedCostDefinition.class)
public abstract class AllocatedCostDefinition implements IdProvider {

    public abstract EntityReference allocationScheme();
    public abstract EntityReference sourceCostKind();
    public abstract EntityReference targetCostKind();

}
