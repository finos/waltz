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
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.application.ApplicationKind;
import org.finos.waltz.model.application.LifecyclePhase;
import org.immutables.value.Value;

import java.sql.Timestamp;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationAttestationInstanceSummary.class)
@JsonDeserialize(as = ImmutableApplicationAttestationInstanceSummary.class)
public abstract class ApplicationAttestationInstanceSummary {

    public abstract EntityReference appRef();
    public abstract String appAssetCode();
    public abstract ApplicationKind appKind();
    public abstract Criticality appCriticality();
    public abstract LifecyclePhase appLifecyclePhase();

    @Nullable
    public abstract Timestamp attestedAt();

    @Nullable
    public abstract String attestedBy();

}

