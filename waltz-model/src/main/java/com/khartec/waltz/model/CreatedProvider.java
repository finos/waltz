package com.khartec.waltz.model;


import com.khartec.waltz.common.DateTimeUtilities;
import org.immutables.value.Value;

import java.time.LocalDateTime;

public interface CreatedProvider {

    @Value.Default
    default LocalDateTime createdAt() {
        return DateTimeUtilities.nowUtc();
    }

    String createdBy();
}
