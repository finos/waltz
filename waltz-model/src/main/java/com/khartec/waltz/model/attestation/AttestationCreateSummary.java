package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationCreateSummary.class)
@JsonDeserialize(as = ImmutableAttestationCreateSummary.class)
public abstract class AttestationCreateSummary {
    public abstract int entityCount();
    public abstract int instanceCount();
    public abstract long recipientCount();
}
