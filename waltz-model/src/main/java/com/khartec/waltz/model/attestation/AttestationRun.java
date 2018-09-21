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

package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.model.EntityReference.mkRef;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationRun.class)
@JsonDeserialize(as = ImmutableAttestationRun.class)
public abstract class AttestationRun implements IdProvider, NameProvider, DescriptionProvider, WaltzEntity {

    public abstract EntityKind targetEntityKind();
    public abstract IdSelectionOptions selectionOptions();
    public abstract Set<Long> involvementKindIds();

    public abstract EntityKind attestedEntityKind();
    public abstract Optional<EntityReference> attestedEntityRef();

    public abstract String issuedBy();

    @Value.Default
    public LocalDate issuedOn() {
        return LocalDate.now();
    }

    public abstract LocalDate dueDate();


    @Override
    @Value.Default
    public EntityReference entityReference() {
        return mkRef(EntityKind.ATTESTATION_RUN, id().get());
    }
}
