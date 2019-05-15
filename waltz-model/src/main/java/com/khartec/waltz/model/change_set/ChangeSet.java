/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.model.change_set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableChangeSet.class)
@JsonDeserialize(as = ImmutableChangeSet.class)
public abstract class ChangeSet implements
        IdProvider,
        EntityKindProvider,
        EntityLifecycleStatusProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        ExternalIdProvider,
        ProvenanceProvider,
        WaltzEntity {

    public abstract Optional<EntityReference> parentEntity();

    public abstract Optional<LocalDateTime> plannedDate();

    @Value.Default
    public EntityKind kind() { return EntityKind.CHANGE_SET; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.CHANGE_SET)
                .id(id().get())
                .name(name())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
