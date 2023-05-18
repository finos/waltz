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

package org.finos.waltz.model.measurable;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.immutables.value.Value;

/**
 * A Measurable represents a specific instance of an item in a hierarchical taxonomy.
 * Measurables may be classified as concrete, in which case applications should
 * be allowed to map directly to this Measurable.  Non concrete measurables should
 * not allow mappings to be linked to them.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMeasurable.class)
@JsonDeserialize(as = ImmutableMeasurable.class)
public abstract class Measurable implements
        EntityKindProvider,
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ParentIdProvider,
        ExternalIdProvider,
        EntityLifecycleStatusProvider,
        ExternalParentIdProvider,
        LastUpdatedProvider,
        ProvenanceProvider,
        PositionProvider,
        WaltzEntity {

    /**
     * The category to which this measurable belongs
     * @return id which references a {@link MeasurableCategory}
     */
    public abstract long categoryId();

    /**
     * A flag which indicates if this measurable may be used in application ratings.
     * @return true if allowed
     */
    public abstract boolean concrete();

    /**
     * Owning organisational unit id (maybe null)
     * @return
     */
    @Nullable
    public abstract Long organisationalUnitId();

    @Value.Default
    public EntityKind kind() { return EntityKind.MEASURABLE; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.MEASURABLE)
                .id(id().get())
                .name(name())
                .externalId(externalId())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
