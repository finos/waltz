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

package com.khartec.waltz.model.enduserapp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.LifecyclePhase;
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
        ProvenanceProvider {

    public abstract Long organisationalUnitId();
    public abstract String applicationKind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract Criticality riskRating();

    @Value.Default
    public String provenance() {
        return "waltz";
    }

    @Value.Default
    public EntityKind kind() { return EntityKind.END_USER_APPLICATION; }

    public EntityReference toEntityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.END_USER_APPLICATION)
                .id(id().get())
                .name(name())
                .build();
    }
}