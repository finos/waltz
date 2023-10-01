/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationRunRecipient.class)
public abstract class AttestationRunRecipient implements IdProvider {

    public abstract long pendingCount();
    public abstract long completedCount();

    @Value.Default
    public long totalCount() {
        return pendingCount() + completedCount();
    }

    public abstract String userId();
}
