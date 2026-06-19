package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationPrimaryFlagSetting.class)
@JsonDeserialize(as = ImmutableAttestationPrimaryFlagSetting.class)
public abstract class AttestationPrimaryFlagSetting {
    @JsonProperty("measurable_category_id")
    public abstract Long measurableCategoryId();
    @JsonProperty("is_primary_mandatory")
    public abstract Boolean isPrimaryMandatory();
}
