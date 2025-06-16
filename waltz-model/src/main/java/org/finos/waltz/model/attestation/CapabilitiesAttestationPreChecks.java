package org.finos.waltz.model.attestation;

import org.immutables.value.Value;

@Value.Immutable
public abstract class CapabilitiesAttestationPreChecks {
    public abstract int mappingCount();
    public abstract int zeroAllocationCount();
    public abstract int totalAllocation();
    public abstract int nonConcreteCount();
}
