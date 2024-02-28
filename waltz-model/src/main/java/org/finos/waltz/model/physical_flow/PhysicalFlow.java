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

package org.finos.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;


/**
 * Binds a data specification (e.g. a file specification) to a logical data flow.
 * As such it can be thought of a realisation of the logical into the
 * physical. A Logical may have many such realisations whereas a a physical
 * data flow may only associated to a single logical flow.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlow.class)
@JsonDeserialize(as = ImmutablePhysicalFlow.class)
public abstract class PhysicalFlow implements
        IdProvider,
        NameProvider,
        IsRemovedProvider,
        CreatedUserTimestampProvider,
        DescriptionProvider,
        ProvenanceProvider,
        LastUpdatedProvider,
        LastAttestedProvider,
        EntityLifecycleStatusProvider,
        ExternalIdProvider,
        WaltzEntity,
        EntityKindProvider
{

    public abstract long logicalFlowId();

    public abstract long specificationId();

    public abstract FrequencyKindValue frequency();

    // e.g. for representing T+0, T+1, T+7, T-1
    public abstract int basisOffset();

    @Value.Default
    public TransportKindValue transport() {
        return TransportKindValue.UNKNOWN;
    }

    @Value.Default
    public FreshnessIndicator freshnessIndicator() {
        return FreshnessIndicator.NEVER_OBSERVED;
    }

    public abstract Optional<Long> specificationDefinitionId();

    public abstract CriticalityValue criticality();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.PHYSICAL_FLOW;
    }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.PHYSICAL_FLOW)
                .id(id().get())
                .description(description())
                .build();
    }

    @Value.Default
    public boolean isReadOnly() { return false; }
}
