package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableTallyPack.class)
@JsonDeserialize(as = ImmutableTallyPack.class)
public abstract class TallyPack<T> {

    public abstract EntityReference entityReference();
    public abstract List<Tally<T>> tallies();

    @Value.Default
    public LocalDateTime lastUpdatedAt() {
        return DateTimeUtilities.nowUtc();
    }

}
