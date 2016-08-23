package com.khartec.waltz.model.datatype_source_sink;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.Rating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataTypeSourceSinks.class)
@JsonDeserialize(as = ImmutableDataTypeSourceSinks.class)
public abstract class DataTypeSourceSinks {

    public abstract EntityReference source();
    public abstract EntityReference organisationalUnit();
    public abstract EntityReference dataType();
    public abstract Rating rating();
    public abstract int sinkCount();
}
