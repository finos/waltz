package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationRun.class)
@JsonDeserialize(as = ImmutableAttestationRun.class)
public abstract class AttestationRun implements IdProvider, NameProvider, DescriptionProvider {

    public abstract EntityKind targetEntityKind();
    public abstract IdSelectionOptions selectionOptions();
    public abstract Set<Long> involvementKindIds();

    public abstract String issuedBy();

    @Value.Default
    public LocalDate issuedOn() {
        return LocalDate.now();
    }
    public abstract LocalDate dueDate();
}
