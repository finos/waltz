package com.khartec.waltz.web.action;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdatePhysicalSpecDataTypesAction.class)
@JsonDeserialize(as = ImmutableUpdatePhysicalSpecDataTypesAction.class)
public abstract class UpdatePhysicalSpecDataTypesAction {

    public abstract long specificationId();
    public abstract Set<Long> addedDataTypeIds();
    public abstract Set<Long> removedDataTypeIds();

}
