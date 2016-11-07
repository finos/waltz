package com.khartec.waltz.model.logical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.TallyPack;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableLogicalFlowStatistics.class)
@JsonDeserialize(as = ImmutableLogicalFlowStatistics.class)
public abstract class LogicalFlowStatistics implements SummaryStatistics {

    public abstract LogicalFlowMeasures appCounts();
    public abstract LogicalFlowMeasures flowCounts();

    public abstract List<TallyPack<String>> dataTypeCounts();
}
