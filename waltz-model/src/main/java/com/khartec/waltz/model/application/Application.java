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


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.rating.RagRating;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableApplication.class)
@JsonDeserialize(as = ImmutableApplication.class)
public abstract class Application implements
        EntityKindProvider,
        IdProvider,
        IsRemovedProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        WaltzEntity,
        EntityLifecycleStatusProvider,
        ExternalIdProvider,
        OrganisationalUnitIdProvider {

    public abstract Optional<String> assetCode();
    public abstract Optional<String> parentAssetCode();
    public abstract ApplicationKind applicationKind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract RagRating overallRating();
    public abstract Optional<LocalDateTime> plannedRetirementDate();
    public abstract Optional<LocalDateTime> actualRetirementDate();
    public abstract Optional<LocalDateTime> commissionDate();


    @Value.Default
    public EntityKind kind() { return EntityKind.APPLICATION; }


    @Value.Default
    public String provenance() {
        return "waltz";
    }


    @Override
    @Value.Derived
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Optional<String> externalId() { return assetCode(); }


    @Value.Default
    public Criticality businessCriticality() { return Criticality.UNKNOWN; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id().get())
                .name(name())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
