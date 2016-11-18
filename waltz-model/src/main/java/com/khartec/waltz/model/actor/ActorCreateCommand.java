package com.khartec.waltz.model.actor;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableActorCreateCommand.class)
@JsonDeserialize(as = ImmutableActorCreateCommand.class)
public abstract class ActorCreateCommand implements NameProvider, DescriptionProvider {

    public abstract boolean isExternal();
}
