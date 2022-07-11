package org.finos.waltz.web.json;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableColumnDescriptor.class)
@JsonDeserialize(as = ImmutableColumnDescriptor.class)
@JsonPropertyOrder({"id","name"})
public interface ColumnDescriptor {

    String id();
    String name();
}
