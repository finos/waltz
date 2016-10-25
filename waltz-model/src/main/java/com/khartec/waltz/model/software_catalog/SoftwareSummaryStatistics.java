package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.Tally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwareSummaryStatistics.class)
@JsonDeserialize(as = ImmutableSoftwareSummaryStatistics.class)
public abstract class SoftwareSummaryStatistics implements SummaryStatistics {

    public abstract List<Tally<String>> vendorCounts();
    public abstract List<Tally<String>> maturityCounts();

}
