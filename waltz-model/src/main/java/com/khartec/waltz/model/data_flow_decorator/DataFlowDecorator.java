package com.khartec.waltz.model.data_flow_decorator;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.authoritativesource.Rating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataFlowDecorator.class)
@JsonDeserialize(as = ImmutableDataFlowDecorator.class)
public abstract class DataFlowDecorator implements ProvenanceProvider {

    public abstract long dataFlowId();
    public abstract EntityReference decoratorEntity();
    public abstract Rating rating();

}
