package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwareCatalog.class)
@JsonDeserialize(as = ImmutableSoftwareCatalog.class)
public abstract class SoftwareCatalog {

    public abstract List<SoftwareUsage> usages();
    public abstract List<SoftwarePackage> packages();

}
