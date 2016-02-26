package com.khartec.waltz.model.trait;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IconProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTrait.class)
@JsonDeserialize(as = ImmutableTrait.class)
public abstract class Trait implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        IconProvider {
}
