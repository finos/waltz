package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwarePackage.class)
@JsonDeserialize(as = ImmutableSoftwarePackage.class)
public abstract class SoftwarePackage implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ExternalIdProvider,
        ProvenanceProvider {

    public abstract String vendor();
    public abstract String version();
    public abstract MaturityStatus maturityStatus();
    public abstract boolean isNotable();

}
