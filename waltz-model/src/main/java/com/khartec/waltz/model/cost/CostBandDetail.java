package com.khartec.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableCostBandDetail.class)
@JsonDeserialize(as = ImmutableCostBandDetail.class)
public abstract class CostBandDetail {

    public abstract CostBand band();
    public abstract List<ApplicationCost> costs();


}
