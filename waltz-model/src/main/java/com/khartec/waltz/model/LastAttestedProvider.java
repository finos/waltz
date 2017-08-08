package com.khartec.waltz.model;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LastAttestedProvider {

    Optional<LocalDateTime> lastAttestedAt();
    Optional<String> lastAttestedBy();
}
