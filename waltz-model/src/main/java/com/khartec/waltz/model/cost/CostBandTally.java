package com.khartec.waltz.model.cost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.tally.Tally;
import org.immutables.value.Value;


/*
 * Non-generic subclass required for serialisation
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCostBandTally.class)
@JsonDeserialize(as = ImmutableCostBandTally.class)
public abstract class CostBandTally implements Tally<CostBand> {

}
