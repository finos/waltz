package org.finos.waltz.service.report_grid;

import org.immutables.value.Value;

@Value.Immutable
public abstract class CellResult {

    public abstract String value();

    public abstract String optionCode();

    public abstract String optionText();


    public static CellResult mkResult(String value, String optionText, String optionCode) {
        return ImmutableCellResult
                .builder()
                .value(value)
                .optionCode(optionCode)
                .optionText(optionText)
                .build();
    }
} 
 



