package com.khartec.waltz.model.checkpoint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.Quarter;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCheckpoint.class)
@JsonDeserialize(as = ImmutableCheckpoint.class)
public abstract class Checkpoint implements
        IdProvider,
        NameProvider,
        DescriptionProvider {

    public abstract int year();
    public abstract Quarter quarter();

}
