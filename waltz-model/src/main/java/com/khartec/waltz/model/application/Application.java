/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.application;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.capabilityrating.RagRating;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableApplication.class)
@JsonDeserialize(as = ImmutableApplication.class)
public abstract class Application implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider {

    public abstract Optional<String> assetCode();
    public abstract Optional<String> parentAssetCode();
    public abstract Long organisationalUnitId();
    public abstract ApplicationKind kind();
    public abstract LifecyclePhase lifecyclePhase();
    public abstract RagRating overallRating();


    @Value.Default
    public String provenance() {
        return "waltz";
    }


    @Value.Default
    public Criticality criticality() { return Criticality.UNKNOWN; }


    public EntityReference toEntityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(id().get())
                .name(name())
                .build();
    }
}
