package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableGrid.class)
@JsonDeserialize(as = ImmutableGrid.class)
public abstract class Grid {


    @Value.Default
    public List<Row> rows() {
        return new ArrayList<>();
    }

}
