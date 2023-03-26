package org.finos.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableCostKindWithYears.class)
public abstract class CostKindWithYears {

    public abstract EntityCostKind costKind();

    public abstract List<Integer> years();

}
