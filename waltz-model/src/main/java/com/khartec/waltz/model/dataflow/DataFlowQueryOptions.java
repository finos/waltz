package com.khartec.waltz.model.dataflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDataFlowQueryOptions.class)
@JsonDeserialize(as = ImmutableDataFlowQueryOptions.class)
public abstract class DataFlowQueryOptions {
   public abstract List<Long> applicationIds();
}
