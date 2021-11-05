package org.finos.waltz.model.attestation;

import org.immutables.value.Value;

@Value.Immutable
public abstract class LogicalFlowAttestationPreChecks {

    public abstract int flowCount();
    public abstract int deprecatedCount();
    public abstract int unknownCount();
    public abstract boolean exemptFromUnknownCheck();
    public abstract boolean exemptFromDeprecatedCheck();
    public abstract boolean exemptFromFlowCountCheck();

}
