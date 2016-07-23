package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticQueryOptions.class)
@JsonDeserialize(as = ImmutableEntityStatisticQueryOptions.class)
public abstract class EntityStatisticQueryOptions {

    public abstract ApplicationIdSelectionOptions selector();
    public abstract List<Long> statisticIds();

}
