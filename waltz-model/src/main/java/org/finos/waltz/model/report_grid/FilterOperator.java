package org.finos.waltz.model.report_grid;

import java.util.Optional;

public enum FilterOperator {
    CONTAINS_ANY_OPTION,
    CONTAINS_ANY_STRING;

    public static Optional<FilterOperator> parseString(String operator) {
        if (operator.equalsIgnoreCase(CONTAINS_ANY_OPTION.name())) {
            return Optional.of(CONTAINS_ANY_OPTION);
        } else if (operator.equalsIgnoreCase(CONTAINS_ANY_STRING.name())) {
            return Optional.of(CONTAINS_ANY_STRING);
        } else {
            return Optional.empty();
        }
    }
}
