package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;


/**
 * Binds a data article (e.g. a file specification) to a logical data flow.
 * As such it can be thought of a realisation of the logical into the
 * physical. A Logical may have many such realisations whereas a a physical
 * data flow may only associated to a single logical flow.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlow.class)
@JsonDeserialize(as = ImmutablePhysicalFlow.class)
public abstract class PhysicalFlow implements
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
