package com.khartec.waltz.model;

import org.immutables.value.Value;

public interface IsReadOnlyProvider {

    @Value.Default
    default boolean isReadOnly() { return false; }

}
