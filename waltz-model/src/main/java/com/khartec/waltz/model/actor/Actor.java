package com.khartec.waltz.model.actor;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableActor.class)
@JsonDeserialize(as = ImmutableActor.class)
public abstract class Actor implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider {
}
