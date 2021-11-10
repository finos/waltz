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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Criticality;
import org.finos.waltz.model.application.LifecyclePhase;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationAttestationSummaryFilters.class)
@JsonDeserialize(as = ImmutableApplicationAttestationSummaryFilters.class)
public abstract class ApplicationAttestationSummaryFilters {

    public abstract Optional<LocalDate> attestationsFromDate();
    public abstract Optional<LifecyclePhase> appLifecyclePhase();
    public abstract Optional<Criticality> appCriticality();
    public abstract Optional<AttestationState> attestationState();

}

