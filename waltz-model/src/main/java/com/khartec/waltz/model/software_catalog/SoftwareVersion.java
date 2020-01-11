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

package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwareVersion.class)
@JsonDeserialize(as = ImmutableSoftwareVersion.class)
public abstract class SoftwareVersion implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ExternalIdProvider,
        ProvenanceProvider,
        CreatedUserTimestampProvider,
        EntityKindProvider,
        WaltzEntity {

    public abstract long softwarePackageId();
    public abstract String version();
    public abstract LocalDate releaseDate();

    @Value.Default
    public String name() { return version(); }

    @Value.Default
    public EntityKind kind() { return EntityKind.SOFTWARE_VERSION; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.SOFTWARE_VERSION)
                .id(id().get())
                .name(version())
                .description(description())
                .build();
    }
}
