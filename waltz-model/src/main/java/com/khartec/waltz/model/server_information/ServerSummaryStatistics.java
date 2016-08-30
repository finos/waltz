package com.khartec.waltz.model.server_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.Tally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as=ImmutableServerSummaryStatistics.class)
@JsonDeserialize(as=ImmutableServerSummaryStatistics.class)
public abstract class ServerSummaryStatistics implements SummaryStatistics {

    public abstract long virtualCount();
    public abstract long physicalCount();
    public abstract List<Tally<String>> environmentCounts();
    public abstract List<Tally<String>> operatingSystemCounts();
    public abstract List<Tally<String>> locationCounts();

    @Value.Default
    public long totalCount() {
        return virtualCount() + physicalCount();
    }
}
