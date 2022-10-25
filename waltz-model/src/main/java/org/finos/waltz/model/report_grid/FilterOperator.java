package org.finos.waltz.model.report_grid;

import java.util.Optional;

import static org.finos.waltz.common.OptionalUtilities.ofExplodable;

public enum FilterOperator {
    CONTAINS_ANY_OPTION,
    CONTAINS_ANY_STRING;

    // --------------------------

    public static Optional<FilterOperator> parseString(String operator) {
        return ofExplodable(() -> valueOf(operator.toUpperCase()));
    }
}
