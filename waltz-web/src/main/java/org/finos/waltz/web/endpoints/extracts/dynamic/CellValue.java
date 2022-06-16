package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableCellValue.class)
@JsonDeserialize(as = ImmutableCellValue.class)
public interface CellValue extends Cell {

    String TYPE = ApiTypes.VALCELL;


    @Value.Default
    default String type() {
        return TYPE;
    }

    String name();
    String value();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Optional<String> comment();
}
