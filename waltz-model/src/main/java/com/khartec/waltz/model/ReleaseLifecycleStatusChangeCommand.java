package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReleaseLifecycleStatusChangeCommand.class)
@JsonDeserialize(as = ImmutableReleaseLifecycleStatusChangeCommand.class)
public abstract class ReleaseLifecycleStatusChangeCommand {

    public abstract ReleaseLifecycleStatus newStatus();
}
