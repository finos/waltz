package com.khartec.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAssetCostStatistics.class)
@JsonDeserialize(as = ImmutableAssetCostStatistics.class)
public abstract class AssetCostStatistics implements SummaryStatistics {
    public abstract List<CostBandTally> costBandCounts();
    public abstract Cost totalCost();
}
