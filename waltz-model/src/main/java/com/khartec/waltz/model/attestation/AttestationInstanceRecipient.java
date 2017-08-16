package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationInstanceRecipient.class)
@JsonDeserialize(as = ImmutableAttestationInstanceRecipient.class)
public abstract class AttestationInstanceRecipient implements IdProvider {

    public abstract AttestationInstance attestationInstance();
    public abstract String userId();
}
