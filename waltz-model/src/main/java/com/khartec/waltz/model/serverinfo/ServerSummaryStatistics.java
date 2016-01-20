package com.khartec.waltz.model.serverinfo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableServerSummaryStatistics.class)
@JsonDeserialize(as=ImmutableServerSummaryStatistics.class)
public abstract class ServerSummaryStatistics {

    public abstract long virtualCount();
    public abstract long physicalCount();

    public long totalCount() {
        return virtualCount() + physicalCount();
    }
}
