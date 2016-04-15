package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwareUsage.class)
@JsonDeserialize(as = ImmutableSoftwareUsage.class)
public abstract class SoftwareUsage implements ProvenanceProvider {

    public abstract long applicationId();
    public abstract long softwarePackageId();

}
