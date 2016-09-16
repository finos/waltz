package com.khartec.waltz.model.database_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.Tally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDatabaseSummaryStatistics.class)
@JsonDeserialize(as = ImmutableDatabaseSummaryStatistics.class)
public abstract class DatabaseSummaryStatistics implements SummaryStatistics {

    public abstract List<Tally<String>> environmentCounts();
    public abstract List<Tally<String>> vendorCounts();
    public abstract List<Tally<String>> endOfLifeStatusCounts();


}
