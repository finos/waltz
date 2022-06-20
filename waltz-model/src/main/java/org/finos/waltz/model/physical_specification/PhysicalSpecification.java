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

package org.finos.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

/**
 * Represents something which is produced by an owning system.
 * This may be a message, file, document or similar.
 * The specification is discrete from the physical
 * flow to better represent systems which distribute a
 * single file to many consumers
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecification.class)
@JsonDeserialize(as = ImmutablePhysicalSpecification.class)
public abstract class PhysicalSpecification implements
        IdProvider,
        IsRemovedProvider,
        CreatedUserTimestampProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        LastUpdatedProvider,
        WaltzEntity {

    public abstract EntityReference owningEntity();

    public abstract DataFormatKindValue format();

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.PHYSICAL_SPECIFICATION)
                .id(id().get())
                .name(name() + externalId().map(extId -> " (" + extId + ")").orElse(""))
                .externalId(externalId())
                .description(description())
                .build();
    }

}
