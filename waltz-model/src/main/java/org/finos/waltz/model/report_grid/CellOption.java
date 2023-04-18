package org.finos.waltz.model.report_grid;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCellOption.class)
@JsonDeserialize(as = ImmutableCellOption.class)
public abstract class CellOption {

    public abstract String text();

    public abstract String code();


    public static CellOption defaultCellOption() {
        return ImmutableCellOption.builder()
                .text("Provided")
                .code("PROVIDED")
                .build();
    }

    public static CellOption mkCellOption(String code, String text) {
        return ImmutableCellOption
                .builder()
                .text(text)
                .code(code)
                .build();
    }

}
