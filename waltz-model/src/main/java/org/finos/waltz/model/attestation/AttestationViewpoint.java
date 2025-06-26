package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationViewpoint.class)
@JsonDeserialize(as = ImmutableAttestationViewpoint.class)
public abstract class AttestationViewpoint {
    public abstract String categoryName();
    public abstract long categoryId();

}
