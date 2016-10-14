package com.khartec.waltz.model;

import com.khartec.waltz.common.DateTimeUtilities;
import org.immutables.value.Value;

import java.time.LocalDateTime;

public interface LastUpdatedProvider {

    @Value.Default
    default LocalDateTime lastUpdatedAt() {
        return DateTimeUtilities.nowUtc();
    }

    String lastUpdatedBy();
}
