/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Value.Immutable
@JsonSerialize(as = ImmutableAttestation.class)
@JsonDeserialize(as = ImmutableAttestation.class)
public abstract class Attestation implements IdProvider, ProvenanceProvider
{
    public abstract EntityReference entityReference();
    public abstract AttestationType attestationType();
    public abstract String attestedBy();

    @Value.Default
    public LocalDateTime attestedAt() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    @Nullable
    public abstract String comments();

}
