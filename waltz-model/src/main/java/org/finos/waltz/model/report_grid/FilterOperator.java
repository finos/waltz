package org.finos.waltz.model.report_grid;

import java.util.Optional;

public enum FilterOperator {
    CONTAINS_OPTION,
    CONTAINS_STRING;

    public static Optional<FilterOperator> parseString(String operator) {
        if (operator.equalsIgnoreCase(CONTAINS_OPTION.name())) {
            return Optional.of(CONTAINS_OPTION);
        } else if (operator.equalsIgnoreCase(CONTAINS_STRING.name())) {
            return Optional.of(CONTAINS_STRING);
        } else {
            return Optional.empty();
        }
    }
}
