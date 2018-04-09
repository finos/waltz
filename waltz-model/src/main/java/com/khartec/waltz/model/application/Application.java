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

package com.khartec.waltz.model.application;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.rating.RagRating;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableApplication.class)
@JsonDeserialize(as = ImmutableApplication.class)
public abstract class Application implements
        EntityKindProvider,
        IdProvider,
        IsRemovedProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        WaltzEntity,
        EntityLifecycleStatusProvider,
        OrganisationalUnitIdProvider {

    public abstract Optional<String> assetCode();
    public abstract Optional<String> parentAssetCode();
    public abstract ApplicationKind applicationKind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract RagRating overallRating();
    public abstract Optional<LocalDateTime> plannedRetirementDate();
    public abstract Optional<LocalDateTime> actualRetirementDate();


    @Value.Default
    public EntityKind kind() { return EntityKind.APPLICATION; }


    @Value.Default
    public String provenance() {
        return "waltz";
    }


    @Value.Default
    public Criticality businessCriticality() { return Criticality.UNKNOWN; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id().get())
                .name(name())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
