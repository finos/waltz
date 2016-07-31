package com.khartec.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAssetCostQueryOptions.class)
@JsonDeserialize(as = ImmutableAssetCostQueryOptions.class)
public abstract class AssetCostQueryOptions {

    public abstract int year();
    public abstract IdSelectionOptions idSelectionOptions();

}
