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

package org.finos.waltz.model.enduserapp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.physical_flow.CriticalityValue;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEndUserApplication.class)
@JsonDeserialize(as = ImmutableEndUserApplication.class)
public abstract class EndUserApplication implements
        EntityKindProvider,
        IdProvider,
        DescriptionProvider,
        ExternalIdProvider,
        NameProvider,
        ProvenanceProvider,
        WaltzEntity {
    public abstract Long organisationalUnitId();
    public abstract String applicationKind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract CriticalityValue riskRating();
    public abstract Boolean isPromoted();

    @Value.Default
    public EntityKind kind() { return EntityKind.END_USER_APPLICATION; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.END_USER_APPLICATION)
                .id(id().get())
                .name(name())
                .description(description())
                .build();
    }
}