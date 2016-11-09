package com.khartec.waltz.model.application_capability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.capability.Capability;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAppCapabilityUsage.class)
@JsonDeserialize(as = ImmutableAppCapabilityUsage.class)
public abstract class AppCapabilityUsage {

    public abstract long applicationId();

    public abstract List<Capability> allCapabilities();
    public abstract List<Long> explicitCapabilityIds();
    public abstract List<Long> primaryCapabilityIds();

}
