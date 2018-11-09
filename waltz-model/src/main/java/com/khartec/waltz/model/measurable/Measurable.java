/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.measurable;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
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
        ExternalParentIdProvider,
        LastUpdatedProvider,
        ProvenanceProvider,
        WaltzEntity {

    /**
     * The category to which this measurable belongs
     * @return id which references a {@link com.khartec.waltz.model.measurable_category.MeasurableCategory}
     */
    public abstract long categoryId();

    /**
     * A flag which indicates if this measurable may be used in application ratings.
     * @return true if allowed
     */
    public abstract boolean concrete();

    @Value.Default
    public EntityKind kind() { return EntityKind.MEASURABLE; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.MEASURABLE)
                .id(id().get())
                .name(name())
                .description(description())
                .build();
    }
}
