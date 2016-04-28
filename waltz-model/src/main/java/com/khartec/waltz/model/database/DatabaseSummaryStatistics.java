package com.khartec.waltz.model.database;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDatabaseSummaryStatistics.class)
@JsonDeserialize(as = ImmutableDatabaseSummaryStatistics.class)
public abstract class DatabaseSummaryStatistics implements SummaryStatistics {

    public abstract List<StringTally> environmentCounts();
    public abstract List<StringTally> vendorCounts();


}
