package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableAttestCommand.class)
@JsonDeserialize(as = ImmutableAttestCommand.class)
public abstract class AttestCommand {

    public abstract Long id();

    @Value
    @Nullable
    public abstract String comments();
}
