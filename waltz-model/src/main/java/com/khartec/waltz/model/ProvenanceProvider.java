package com.khartec.waltz.model;

import org.immutables.value.Value;

public interface ProvenanceProvider {

    @Value.Default
    default String provenance() {
        return "waltz";
    }
}
