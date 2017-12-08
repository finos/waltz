package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationInstance.class)
@JsonDeserialize(as = ImmutableAttestationInstance.class)
public abstract class AttestationInstance implements IdProvider {

    public abstract Long attestationRunId();
    public abstract EntityReference parentEntity();
    public abstract EntityKind childEntityKind();

    public abstract Optional<LocalDateTime> attestedAt();
    public abstract Optional<String> attestedBy();
}

