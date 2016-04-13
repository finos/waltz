package com.khartec.waltz.model;

import java.util.Optional;

public interface ExternalIdProvider {

    Optional<String> externalId();

}
