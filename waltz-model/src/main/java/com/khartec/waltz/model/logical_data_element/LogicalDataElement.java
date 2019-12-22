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

package com.khartec.waltz.model.logical_data_element;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLogicalDataElement.class)
@JsonDeserialize(as = ImmutableLogicalDataElement.class)
public abstract class LogicalDataElement implements
        IdProvider,
        EntityKindProvider,
        EntityLifecycleStatusProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        WaltzEntity {

    @Value.Default
    public EntityKind kind() { return EntityKind.LOGICAL_DATA_ELEMENT; }


    public abstract FieldDataType type();
    public abstract long parentDataTypeId();

    @Value.Default
    public String provenance() {
        return "waltz";
    }


    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.LOGICAL_DATA_ELEMENT)
                .id(id().get())
                .name(name())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
