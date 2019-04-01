package com.khartec.waltz.model.allocation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableAllocation.class)
@JsonDeserialize(as = ImmutableAllocation.class)
public abstract class Allocation implements
        LastUpdatedProvider,
        ProvenanceProvider,
        ExternalIdProvider {

    public abstract long schemeId();
    public abstract long measurableId();
    public abstract EntityReference entityReference();
    public abstract BigDecimal percentage();
    public abstract boolean isFixed();

}
