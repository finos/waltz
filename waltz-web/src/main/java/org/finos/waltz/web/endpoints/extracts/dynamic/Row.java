package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableRow.class)
@JsonDeserialize(as = ImmutableRow.class)
public abstract class Row {

    public abstract KeyCell id();

    @Value.Default
    public List<Cell> cells() {
        return new ArrayList<>();
    }
}
