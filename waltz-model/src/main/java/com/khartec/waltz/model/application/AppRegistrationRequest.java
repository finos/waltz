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

package com.khartec.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.rating.RagRating;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;


@Value.Immutable
@JsonSerialize(as = ImmutableAppRegistrationRequest.class)
@JsonDeserialize(as = ImmutableAppRegistrationRequest.class)
public abstract class AppRegistrationRequest {

    public abstract String name();
    public abstract Optional<String> description();
    public abstract long organisationalUnitId();
    public abstract ApplicationKind applicationKind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract Optional<String> assetCode();
    public abstract Optional<String> parentAssetCode();
    public abstract Set<String> aliases();
    public abstract Set<String> tags();
    public abstract RagRating overallRating();
    public abstract Criticality businessCriticality();
    public abstract Optional<String> provenance();

}
