package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableKeyCell.class)
@JsonDeserialize(as = ImmutableKeyCell.class)
public interface KeyCell extends Cell {

    String TYPE =ApiTypes.KEYCELL;

    @Value.Default
    default String type() {
        return TYPE;
    }


    EntityReference key();

}
