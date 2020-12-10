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

package com.khartec.waltz.model.involvement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableInvolvement.class)
@JsonDeserialize(as = ImmutableInvolvement.class)
public abstract class Involvement implements ProvenanceProvider {

    public abstract long kindId();
    public abstract EntityReference entityReference();
    public abstract String employeeId();

    @Value.Default
    public boolean isReadOnly() { return true; }

    @Value.Default
    public String provenance() {
        return "waltz";
    }


    public static Involvement mkInvolvement(
            EntityReference entityRef,
            String employeeId,
            int involvementKindId,
            String provenance,
            boolean isReadOnly) {

        return ImmutableInvolvement.builder()
                .entityReference(entityRef)
                .employeeId(employeeId)
                .kindId(involvementKindId)
                .provenance(provenance)
                .isReadOnly(isReadOnly)
                .build();
    }
}
