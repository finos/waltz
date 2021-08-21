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

package com.khartec.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.sql.Timestamp;
import java.util.Date;

@Value.Immutable
@JsonSerialize(as = ImmutableLatestMeasurableAttestationInfo.class)
@JsonDeserialize(as = ImmutableLatestMeasurableAttestationInfo.class)
public abstract class LatestMeasurableAttestationInfo {

    public abstract EntityReference categoryRef();
    public abstract EntityReference attestationInstanceRef();
    public abstract EntityReference attestationRunRef();

    @Nullable
    public abstract Timestamp attestedAt();

    @Nullable
    public abstract String attestedBy();

    public abstract Date dueDate();
    public abstract Date issuedOn();

}

