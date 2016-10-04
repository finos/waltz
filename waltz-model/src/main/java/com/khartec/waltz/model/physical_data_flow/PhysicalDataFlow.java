package com.khartec.waltz.model.physical_data_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalDataFlow.class)
@JsonDeserialize(as = ImmutablePhysicalDataFlow.class)
public abstract class PhysicalDataFlow implements
        IdProvider,
        DescriptionProvider,
        ProvenanceProvider {

    public abstract long flowId();

    public abstract long articleId();

    public abstract FrequencyKind frequency();

    // e.g. for representing T+0, T+1, T+7, T-1
    public abstract int basisOffset();

    public abstract TransportKind transport();
}
