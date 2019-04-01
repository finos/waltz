package com.khartec.waltz.model.allocation_scheme;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAllocationScheme.class)
@JsonDeserialize(as = ImmutableAllocationScheme.class)
public abstract class AllocationScheme implements IdProvider, NameProvider, DescriptionProvider {

    public abstract long measurableCategoryId();

}
