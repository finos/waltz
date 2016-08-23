package com.khartec.waltz.model.server_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableServerSummaryStatistics.class)
@JsonDeserialize(as=ImmutableServerSummaryStatistics.class)
public abstract class ServerSummaryStatistics implements SummaryStatistics {

    public abstract long virtualCount();
    public abstract long physicalCount();
    public abstract List<StringTally> environmentCounts();
    public abstract List<StringTally> operatingSystemCounts();
    public abstract List<StringTally> locationCounts();

    @Value.Default
    public long totalCount() {
        return virtualCount() + physicalCount();
    }
}
