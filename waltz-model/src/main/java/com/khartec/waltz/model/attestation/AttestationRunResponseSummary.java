package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationRunResponseSummary.class)
@JsonDeserialize(as = ImmutableAttestationRunResponseSummary.class)
public abstract class AttestationRunResponseSummary {
    public abstract long runId();
    public abstract long completeCount();
    public abstract long pendingCount();
}
