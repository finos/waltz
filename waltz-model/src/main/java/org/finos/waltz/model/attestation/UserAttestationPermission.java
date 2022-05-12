package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as=ImmutableUserAttestationPermission.class)
public abstract class UserAttestationPermission {

    public abstract EntityKind subjectKind();
    public abstract EntityReference qualifierReference();

    @Value.Default
    public boolean hasPermission() {
        return false;
    };

}
