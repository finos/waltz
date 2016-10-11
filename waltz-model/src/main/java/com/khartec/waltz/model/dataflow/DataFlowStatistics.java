package com.khartec.waltz.model.dataflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.TallyPack;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDataFlowStatistics.class)
@JsonDeserialize(as = ImmutableDataFlowStatistics.class)
public abstract class DataFlowStatistics implements SummaryStatistics {

    public abstract DataFlowMeasures appCounts();
    public abstract DataFlowMeasures flowCounts();

    public abstract List<TallyPack<String>> dataTypeCounts();
}
