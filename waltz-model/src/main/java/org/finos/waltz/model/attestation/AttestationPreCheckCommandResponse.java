package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.CommandOutcome;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationPreCheckCommandResponse.class)
@JsonDeserialize(as = ImmutableAttestationPreCheckCommandResponse.class)
public abstract class AttestationPreCheckCommandResponse {
    public abstract CommandOutcome outcome();
    public abstract Optional<String> message();
}