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

package com.khartec.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
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
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        LastUpdatedProvider,
        WaltzEntity {

    public abstract EntityReference owningEntity();
    public abstract DataFormatKind format();

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.PHYSICAL_SPECIFICATION)
                .id(id().get())
                .name(name() + externalId().map(extId -> " (" + extId + ")").orElse(""))
                .description(description())
                .build();
    }

}
